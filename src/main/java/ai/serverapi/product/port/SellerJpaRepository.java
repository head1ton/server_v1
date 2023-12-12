package ai.serverapi.product.port;

import ai.serverapi.member.domain.entity.MemberEntity;
import ai.serverapi.product.domain.entity.SellerEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerJpaRepository extends JpaRepository<SellerEntity, Long> {

    Optional<SellerEntity> findByMember(MemberEntity memberEntity);

    Optional<SellerEntity> findByCompany(String company);
}