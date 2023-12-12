package ai.serverapi.product.repository;

import ai.serverapi.member.domain.model.Member;
import ai.serverapi.product.domain.model.Seller;

public interface SellerRepository {

    Seller findById(Long id);

    Seller findByMember(Member member);

}
