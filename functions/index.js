const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

exports.sendHabitReminders = functions.pubsub.schedule('every 5 minutes').onRun(async (context) => {
  const now = new Date();
  const hh = String(now.getHours()).padStart(2, '0');
  const mm = String(now.getMinutes()).padStart(2, '0');
  const currentTime = `${hh}:${mm}`;

  // Chuyển đổi today thành định dạng yyyyMMdd
  const today = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`;

  // Tính khoảng thời gian cần kiểm tra (5 phút trước và hiện tại)
  const minutes = parseInt(mm);
  const timeRange = [];
  for (let i = 0; i <= 5; i++) {
    const checkMinutes = (minutes - i + 60) % 60; // Xử lý trường hợp qua giờ
    const checkHours = hh - (i > minutes ? 1 : 0); // Giảm giờ nếu cần
    const checkTime = `${String(checkHours).padStart(2, '0')}:${String(checkMinutes).padStart(2, '0')}`;
    timeRange.push(checkTime);
  }

  // Truy vấn thói quen trong khoảng thời gian
  const habitsSnapshot = await db.collection('Habit')
    .where('remind_time', 'in', timeRange)
    .get();

  for (const habitDoc of habitsSnapshot.docs) {
    const habit = habitDoc.data();
    const habitId = habitDoc.id;
    const userId = habit.user_id;

    // Kiểm tra đã có HabitLog hôm nay chưa
    const logSnapshot = await db.collection('HabitLog')
      .where('habit_id', '==', habitId)
      .where('user_id', '==', userId)
      .where('date', '==', today)
      .get();

    if (logSnapshot.empty) {
      // Chưa hoàn thành -> gửi FCM
      const userDoc = await db.collection('User').doc(userId).get();
      const token = userDoc.data()?.fcm_token;

      if (token) {
        await admin.messaging().send({
          token,
          notification: {
            title: '🕒 Nhắc nhở thói quen',
            body: `Đến giờ thực hiện thói quen: ${habit.name}`,
          }
        });
        console.log(`📤 Sent reminder to ${userId} for habit "${habit.name}" at ${currentTime}`);
      }
    }
  }
});
