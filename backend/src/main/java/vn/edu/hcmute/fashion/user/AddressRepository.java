package vn.edu.hcmute.fashion.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    java.util.List<Address> findAllByUserId(Long userId);

    java.util.Optional<Address> findByIdAndUserId(Long id, Long userId);
}
