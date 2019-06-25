package fr.mnhn.herbonautes.cleantilesbatch.specimen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecimenRepository extends JpaRepository<Specimen, Long> {
}
