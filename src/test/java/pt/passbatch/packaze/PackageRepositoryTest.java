package pt.passbatch.packaze;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class PackageRepositoryTest {
    @Autowired
    private PackageRepository packageRepository;

    @Test
    @DisplayName("pk 값이 잘 생성 되는지 검사")
    public void test_save() {
        //given
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("바디 챌린지 PT 12주");
        packageEntity.setPeriod(84);

        //when
        packageRepository.save(packageEntity);

        //then
        assertNotNull(packageEntity.getPackageSeq());
    }

    @Test
    public void test_findByCreatedAtAfter() {
        //given
        LocalDateTime dateTime = LocalDateTime.now().minusMinutes(1); //1분전
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("학생 전용 3개월");
        packageEntity.setPeriod(90);
        packageRepository.save(packageEntity);

        PackageEntity packageEntity1 = new PackageEntity();
        packageEntity1.setPackageName("학생 전용 6개월");
        packageEntity1.setPeriod(180);
        packageRepository.save(packageEntity1);

        //when
        final List<PackageEntity> packageEntities = packageRepository.findByCreatedAtAfter(dateTime, PageRequest.of(0,1, Sort.by("packageSeq").descending()));

        //then
        assertEquals(packageEntities.size(), 1);
        assertEquals(packageEntities.get(0).getPackageSeq(), packageEntity1.getPackageSeq());
    }

    @Test
    public void test_updateCountAndPeriod() {
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("바디프로필 이벤트 4개월");
        packageEntity.setPeriod(90);
        packageRepository.save(packageEntity);

        //when
        int updateCount = packageRepository.updateCountAndPeriod(packageEntity.getPackageSeq(), 30, 120);
        PackageEntity updatedPackageEntity = packageRepository.findById(packageEntity.getPackageSeq()).get();

        //then
        assertEquals(30, updatedPackageEntity.getCount());
        assertEquals(120, updatedPackageEntity.getPeriod());
        assertEquals(1, updateCount);

    }

}