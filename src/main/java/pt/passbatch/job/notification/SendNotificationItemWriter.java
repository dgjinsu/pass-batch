package pt.passbatch.job.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import pt.passbatch.message.KakaoTalkMessageAdapter;
import pt.passbatch.notification.NotificationEntity;
import pt.passbatch.notification.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Component
public class SendNotificationItemWriter implements ItemWriter<NotificationEntity> {
    private final NotificationRepository notificationRepository;
    private final KakaoTalkMessageAdapter kakaoTalkMessageAdapter;

    public SendNotificationItemWriter(NotificationRepository notificationRepository, KakaoTalkMessageAdapter kakaoTalkMessageAdapter) {
        this.notificationRepository = notificationRepository;
        this.kakaoTalkMessageAdapter = kakaoTalkMessageAdapter;
    }


    @Override
    public void write(List<? extends NotificationEntity> items) throws Exception {
        int count = 0;
        for (NotificationEntity notification : items) {
            // 카카오톡 보내기 시도 후 성공 여부 반환
            boolean successful = kakaoTalkMessageAdapter.sendKakaoTalkMessage(notification.getUuid(), notification.getText());
            if(successful) {
                notification.setSent(true);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
                count++;
            }
        }
        log.info("SendNotificationItemWriter - write: 수업 전 알람 {}/{}건 전송 성공", count, items.size());
    }
}
