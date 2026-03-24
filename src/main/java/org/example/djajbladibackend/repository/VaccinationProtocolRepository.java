package org.example.djajbladibackend.repository;

import org.example.djajbladibackend.models.VaccinationProtocol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Boot Best Practice: Repository avec JOIN FETCH pour éviter N+1
 */
@Repository
public interface VaccinationProtocolRepository extends JpaRepository<VaccinationProtocol, Long> {

    List<VaccinationProtocol> findByStrainOrderByDayOfLifeAsc(String strain);

    boolean existsByStrainAndVaccineNameAndDayOfLife(String strain,
                                                     String vaccineName,
                                                     Integer dayOfLife);

    @Query("SELECT vp FROM VaccinationProtocol vp " +
            "LEFT JOIN FETCH vp.createdBy " +
            "WHERE vp.strain = :strain " +
            "ORDER BY vp.dayOfLife ASC")
    List<VaccinationProtocol> findProtocolsByStrain(@Param("strain") String strain);

    @Query("SELECT vp FROM VaccinationProtocol vp " +
            "LEFT JOIN FETCH vp.createdBy " +
            "WHERE vp.id = :id")
    java.util.Optional<VaccinationProtocol> findByIdWithCreatedBy(@Param("id") Long id);
}
