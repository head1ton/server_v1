package ai.serverapi.member.port;

import ai.serverapi.member.domain.entity.MemberApplySellerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberApplySellerJpaRepository extends
    JpaRepository<MemberApplySellerEntity, Long> {

}
