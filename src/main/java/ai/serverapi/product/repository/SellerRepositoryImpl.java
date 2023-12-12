package ai.serverapi.product.repository;

import ai.serverapi.member.domain.entity.MemberEntity;
import ai.serverapi.member.domain.model.Member;
import ai.serverapi.product.domain.entity.SellerEntity;
import ai.serverapi.product.domain.model.Seller;
import ai.serverapi.product.port.SellerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SellerRepositoryImpl implements SellerRepository {

    private final SellerJpaRepository sellerJpaRepository;

    @Override
    public Seller findById(final Long id) {
        SellerEntity sellerEntity = sellerJpaRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException("유효하지 않은 판매자입니다."));
        return sellerEntity.toModel();
    }

    @Override
    public Seller findByMember(final Member member) {
        SellerEntity sellerEntity = sellerJpaRepository.findByMember(MemberEntity.from(member))
                                                       .orElseThrow(
                                                           () -> new IllegalArgumentException(
                                                               "유효하지 않은 판매자입니다."));
        return sellerEntity.toModel();
    }
}
