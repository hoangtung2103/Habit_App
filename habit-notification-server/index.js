const admin = require('firebase-admin');
const cron = require('node-cron');

// Khởi tạo Firebase Admin SDK
const serviceAccount = require('./serviceAccountKey.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Hàm gửi thông báo
async function sendHabitReminders() {
  try {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const currentTime = `${hh}:${mm}`;

    // Định dạng ngày: yyyyMMdd
    const today = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`;

    // Tạo khoảng thời gian kiểm tra (chỉ thời gian hiện tại)
    const timeRange = [currentTime]; // Chỉ kiểm tra phút hiện tại
    // Nếu muốn kiểm tra thêm 1 phút trước (dự phòng):
    // const minutes = parseInt(mm);
    // const totalMinutes = minutes - 1;
    // const checkMinutes = (totalMinutes + 60) % 60;
    // const checkHours = Math.floor((parseInt(hh) + Math.floor((totalMinutes) / 60)) % 24);
    // const prevTime = `${String(checkHours).padStart(2, '0')}:${String(checkMinutes).padStart(2, '0')}`;
    // timeRange.push(prevTime);

    // Chia timeRange thành các nhóm tối đa 10 phần tử (dự phòng cho tương lai)
    const timeRangeChunks = [];
    for (let i = 0; i < timeRange.length; i += 10) {
      timeRangeChunks.push(timeRange.slice(i, i + 10));
    }

    // Lưu trữ tất cả thói quen và user_id
    const habitsToNotify = [];
    for (const chunk of timeRangeChunks) {
      const habitsSnapshot = await db.collection('Habit')
        .where('remind_time', 'in', chunk)
        .get();

      for (const habitDoc of habitsSnapshot.docs) {
        const habit = habitDoc.data();
        const habitId = habitDoc.id;
        const userId = habit.user_id;

        // Kiểm tra HabitLog
        const logSnapshot = await db.collection('HabitLog')
          .where('habit_id', '==', habitId)
          .where('user_id', '==', userId)
          .where('date', '==', today)
          .limit(1) // Chỉ cần 1 bản ghi để kiểm tra
          .get();

        if (logSnapshot.empty) {
          habitsToNotify.push({ habit, habitId, userId });
        }
      }
    }

    // Lấy danh sách user_id duy nhất
    const userIds = [...new Set(habitsToNotify.map(item => item.userId))];
    if (userIds.length === 0) {
      console.log('No habits to notify at', currentTime);
      return;
    }

    // Truy vấn tất cả User cùng lúc
    const userDocs = await db.collection('User')
      .where(admin.firestore.FieldPath.documentId(), 'in', userIds)
      .get();

    const userTokens = new Map();
    userDocs.forEach(doc => {
      const data = doc.data();
      if (data.fcm_token) {
        userTokens.set(doc.id, data.fcm_token);
      }
    });

    // Gửi thông báo
    const messaging = admin.messaging();
    const promises = habitsToNotify.map(async ({ habit, userId }) => {
      const token = userTokens.get(userId);
      if (token) {
        try {
          await messaging.send({
            token,
            notification: {
              title: '🕒 Nhắc nhở thói quen',
              body: `Đến giờ thực hiện thói quen: ${habit.name}`
            }
          });
          console.log(`📤 Gửi nhắc nhở đến ${userId} for thói quen "${habit.name}" vào lúc ${currentTime}`);
        } catch (error) {
          console.error(`Failed to send notification to ${userId} for habit "${habit.name}":`, error.message);
        }
      } else {
        console.log(`No FCM token for user ${userId}`);
      }
    });

    await Promise.all(promises);
  } catch (error) {
    console.error('Error in sendHabitReminders:', error.message);
  }
}

// Lên lịch chạy mỗi 1 phút
cron.schedule('* * * * *', () => {
  console.log('Checking for habit reminders...');
  sendHabitReminders();
});

// Khởi động server
console.log('Habit Notification Server is running...');