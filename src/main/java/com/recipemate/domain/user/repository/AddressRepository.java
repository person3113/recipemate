package com.recipemate.domain.user.repository;

import com.recipemate.domain.user.entity.Address;
import com.recipemate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);

    Optional<Address> findByUserAndIsDefaultTrue(User user);

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.id = :addressId")
    Optional<Address> findByUserIdAndAddressId(@Param("userId") Long userId, @Param("addressId") Long addressId);

    boolean existsByUserAndIsDefaultTrue(User user);
}
