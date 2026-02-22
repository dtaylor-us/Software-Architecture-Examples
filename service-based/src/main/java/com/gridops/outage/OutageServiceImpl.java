package com.gridops.outage;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OutageServiceImpl implements OutageService {

    private final OutageRepository repository;

    public OutageServiceImpl(OutageRepository repository) {
        this.repository = repository;
    }

    @Override
    public OutageRecord reportOutage(ReportOutageCommand command) {
        String id = "OUT-" + UUID.randomUUID().toString().substring(0, 8);
        OutageRecord record = new OutageRecord(
                id,
                command.assetId(),
                command.description(),
                command.startTime(),
                null,
                command.severity(),
                "ACTIVE"
        );
        return repository.save(record);
    }

    @Override
    public Optional<OutageRecord> getOutage(String outageId) {
        return repository.findById(outageId);
    }

    @Override
    public List<OutageRecord> listActiveOutages() {
        return repository.findAllActive();
    }

    @Override
    public List<OutageRecord> listByAsset(String assetId) {
        return repository.findByAssetId(assetId);
    }
}
