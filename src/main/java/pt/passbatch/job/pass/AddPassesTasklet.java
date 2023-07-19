package pt.passbatch.job.pass;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Configuration;
import pt.passbatch.pass.*;
import pt.passbatch.user.UserGroupMappingEntity;
import pt.passbatch.user.UserGroupMappingRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class AddPassesTasklet implements Tasklet {
    private final PassRepository passRepository;
    private final BulkPassRepository bulkPassRepository;
    private final UserGroupMappingRepository userGroupMappingRepository;

    public AddPassesTasklet(PassRepository passRepository, BulkPassRepository bulkPassRepository, UserGroupMappingRepository userGroupMappingRepository) {
        this.passRepository = passRepository;
        this.bulkPassRepository = bulkPassRepository;
        this.userGroupMappingRepository = userGroupMappingRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        //이용권 시작 일시 1일 전 user group 내 각 사용자에게 이용권을 추가
        final LocalDateTime startedAt = LocalDateTime.now().minusDays(1);

        //처리가 안 된 벌크 패스, 하루 전보다 최근인 것
        final List<BulkPassEntity> bulkPassEntities = bulkPassRepository.findByStatusAndStartedAtGreaterThan(BulkPassStatus.READY, startedAt);

        int count = 0;
        for(BulkPassEntity bulkPassEntity : bulkPassEntities) {
            final List<String> userIds = userGroupMappingRepository.findByUserGroupId(bulkPassEntity.getUserGroupId())
                    .stream().map(UserGroupMappingEntity::getUserId).collect(Collectors.toList());
            count += addPasses(bulkPassEntity, userIds);

            bulkPassEntity.setStatus(BulkPassStatus.COMPLETED);
        }

        return RepeatStatus.FINISHED;
    }

    // bulkPass 의 정보로 pass 데이터 생성
    private int addPasses(BulkPassEntity bulkPassEntity, List<String> userIds) {
        List<PassEntity> passEntities = new ArrayList<>();
        for(String userId: userIds) {
            PassEntity pass = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);
            passEntities.add(pass);
        }
        return passRepository.saveAll(passEntities).size();
    }
}
