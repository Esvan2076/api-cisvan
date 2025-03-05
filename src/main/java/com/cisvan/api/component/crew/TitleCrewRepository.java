package com.cisvan.api.component.crew;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleCrewRepository extends JpaRepository<TitleCrew, String> {
}
