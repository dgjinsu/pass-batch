package pt.passbatch.job.pass;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import pt.passbatch.pass.*;
import pt.passbatch.user.UserGroupMappingEntity;
import pt.passbatch.user.UserGroupMappingRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AddPassesTaskletTest {
    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private PassRepository passRepository;

    @Mock
    private BulkPassRepository bulkPassRepository;

    @Mock
    private UserGroupMappingRepository userGroupMappingRepository;
    @InjectMocks
    private AddPassesTasklet addPassesTasklet;

    @Test
    public void test_execute() throws Exception {
        // given
        final String userGroupId = "GROUP";
        final String userId = "A1000000";
        final Integer packageSeq = 1;
        final Integer count = 10;

        final LocalDateTime now = LocalDateTime.now();

        final BulkPassEntity bulkPassEntity = new BulkPassEntity();
        bulkPassEntity.setPackageSeq(packageSeq);
        bulkPassEntity.setUserGroupId(userGroupId);
        bulkPassEntity.setStatus(BulkPassStatus.READY);
        bulkPassEntity.setCount(count);
        bulkPassEntity.setStartedAt(now);
        bulkPassEntity.setEndedAt(now.plusDays(60));

        final UserGroupMappingEntity userGroupMappingEntity = new UserGroupMappingEntity();
        userGroupMappingEntity.setUserGroupId(userGroupId);
        userGroupMappingEntity.setUserId(userId);

        // when

        //ready로 파라미터가 들어가는지도 검사
        when(bulkPassRepository.findByStatusAndStartedAtGreaterThan(eq(BulkPassStatus.READY), any())).thenReturn(List.of(bulkPassEntity));
        when(userGroupMappingRepository.findByUserGroupId(eq("GROUP"))).thenReturn(List.of(userGroupMappingEntity));

        RepeatStatus repeatStatus = addPassesTasklet.execute(stepContribution, chunkContext);

        // then
        // execute의 return 값인 RepeatStatus 값을 확인합니다.
        assertEquals(RepeatStatus.FINISHED, repeatStatus);

        // 추가된 PassEntity 값을 확인합니다.
        // repository 에 추가된 것들만 검사
        ArgumentCaptor<List> passEntitiesCaptor = ArgumentCaptor.forClass(List.class);

        //verify 의 첫번째 인자에는 Mock 객체를 넣고, 두번째 인자는 동작 방법(실행 횟수 등)을 넣는다.
        //조작한 동작을 확인
        verify(passRepository, times(1)).saveAll(passEntitiesCaptor.capture());
        final List<PassEntity> passEntities = passEntitiesCaptor.getValue();

        assertEquals(1, passEntities.size());

        final PassEntity passEntity = passEntities.get(0);
        assertEquals(packageSeq, passEntity.getPackageSeq());
        assertEquals(userId, passEntity.getUserId());
        assertEquals(PassStatus.READY, passEntity.getStatus());
        assertEquals(count, passEntity.getRemainingCount());

    }


}