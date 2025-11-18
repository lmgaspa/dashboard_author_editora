package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence;

import com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity.MonthlyChargeEntity;
import com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity.SpringMonthlyChargeRepository;
import com.dianaglobal.paineldoauthorbackend.application.port.out.MonthlyChargeRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.domain.model.ChargeStatus;
import com.dianaglobal.paineldoauthorbackend.domain.model.MonthlyCharge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaMonthlyChargeRepository implements MonthlyChargeRepositoryPort {

    private final SpringMonthlyChargeRepository repository;

    @Override
    public void save(MonthlyCharge charge) {
        repository.save(MonthlyChargeEntity.fromDomain(charge));
    }

    @Override
    public Optional<MonthlyCharge> findById(UUID id) {
        return repository.findById(id).map(MonthlyChargeEntity::toDomain);
    }

    @Override
    public Optional<MonthlyCharge> findByAuthorIdAndMonthAndYear(String authorId, Integer month, Integer year) {
        return repository.findByAuthorIdAndChargeMonthAndChargeYear(authorId, month, year)
                .map(MonthlyChargeEntity::toDomain);
    }

    @Override
    public List<MonthlyCharge> findAllByAuthorId(String authorId) {
        return repository.findByAuthorId(authorId).stream()
                .map(MonthlyChargeEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyCharge> findAllByAuthorIdAndStatus(String authorId, ChargeStatus status) {
        return repository.findByAuthorIdAndStatus(authorId, status).stream()
                .map(MonthlyChargeEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyCharge> findAllByStatus(ChargeStatus status) {
        return repository.findByStatus(status).stream()
                .map(MonthlyChargeEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyCharge> findAllByDueDateBefore(LocalDate date) {
        return repository.findByDueDateBefore(date).stream()
                .map(MonthlyChargeEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyCharge> findAllByStatusAndDueDateBefore(ChargeStatus status, LocalDate date) {
        return repository.findByStatusAndDueDateBefore(status, date).stream()
                .map(MonthlyChargeEntity::toDomain)
                .collect(Collectors.toList());
    }
}



