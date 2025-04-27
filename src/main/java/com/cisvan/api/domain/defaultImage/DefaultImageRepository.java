package com.cisvan.api.domain.defaultImage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefaultImageRepository extends JpaRepository<DefaultImage, Long> {
    // Aquí puedes agregar métodos personalizados si los necesitas en el futuro
}