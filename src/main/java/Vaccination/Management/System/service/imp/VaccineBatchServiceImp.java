package Vaccination.Management.System.service.imp;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.vaccinebatch.CreateVaccineBatchRequest;
import Vaccination.Management.System.model.dto.vaccinebatch.VaccineBatchResponse;
import Vaccination.Management.System.model.entity.*;
import Vaccination.Management.System.model.enums.BatchStatus;
import Vaccination.Management.System.repository.*;
import Vaccination.Management.System.service.VaccineBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VaccineBatchServiceImp implements VaccineBatchService {

    private final VaccineBatchRepository vaccineBatchRepository;
    private final FacilityRepository facilityRepository;
    private final VaccineRepository vaccineRepository;
    private final UserRepository userRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final SupplierRepository supplierRepository;

    @Override
    @Transactional
    public VaccineBatchResponse createBatch(Long facilityId, CreateVaccineBatchRequest request, Long staffId) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_NOT_FOUND));

        if (!facility.isActive()) {
            throw new AppException(ErrorCode.FACILITY_INACTIVE);
        }

        Vaccine vaccine = vaccineRepository.findById(request.getVaccineId())
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));

        if (!vaccine.isActive()) {
            throw new AppException(ErrorCode.VACCINE_INACTIVE);
        }

        if (vaccineBatchRepository.existsByBatchNumberAndFacilityId(request.getBatchNumber(), facilityId)) {
            throw new AppException(ErrorCode.BATCH_DUPLICATE);
        }

        if (!request.getExpiryDate().isAfter(LocalDate.now())) {
            throw new AppException(ErrorCode.BATCH_UNAVAILABLE);
        }

        Manufacturer manufacturer = null;
        if (request.getManufacturerId() != null) {
            manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                    .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED));
        }

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED));
        }

        VaccineBatch batch = VaccineBatch.builder()
                .vaccine(vaccine)
                .facility(facility)
                .batchNumber(request.getBatchNumber())
                .quantity(request.getQuantity())
                .remaining(request.getQuantity())
                .manufacturedDate(request.getManufacturedDate())
                .expiryDate(request.getExpiryDate())
                .receivedDate(request.getReceivedDate())
                .manufacturer(manufacturer)
                .supplier(supplier)
                .price(request.getPrice())
                .status(BatchStatus.ACTIVE)
                .build();

        batch = vaccineBatchRepository.save(batch);
        return toResponse(batch);
    }

    @Override
    public List<VaccineBatchResponse> getBatchesByFacility(Long facilityId) {
        facilityRepository.findById(facilityId)
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_NOT_FOUND));

        return vaccineBatchRepository.findByFacilityId(facilityId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public VaccineBatchResponse recallBatch(Long batchId, Long staffId) {
        VaccineBatch batch = vaccineBatchRepository.findById(batchId)
                .orElseThrow(() -> new AppException(ErrorCode.BATCH_NOT_FOUND));

        if (batch.getStatus() == BatchStatus.RECALLED) {
            throw new AppException(ErrorCode.BATCH_ALREADY_RECALLED);
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        batch.setStatus(BatchStatus.RECALLED);
        batch.setRecalledAt(LocalDateTime.now());
        batch.setRecalledBy(staff);

        batch = vaccineBatchRepository.save(batch);
        return toResponse(batch);
    }

    // --- Mapper ---

    private VaccineBatchResponse toResponse(VaccineBatch b) {
        return VaccineBatchResponse.builder()
                .id(b.getId())
                .vaccineId(b.getVaccine().getId())
                .vaccineName(b.getVaccine().getName())
                .facilityId(b.getFacility().getId())
                .facilityName(b.getFacility().getName())
                .batchNumber(b.getBatchNumber())
                .quantity(b.getQuantity())
                .remaining(b.getRemaining())
                .status(b.getStatus())
                .manufacturedDate(b.getManufacturedDate())
                .expiryDate(b.getExpiryDate())
                .receivedDate(b.getReceivedDate())
                .manufacturerName(b.getManufacturer() != null ? b.getManufacturer().getName() : null)
                .supplierName(b.getSupplier() != null ? b.getSupplier().getName() : null)
                .price(b.getPrice())
                .importedAt(b.getImportedAt())
                .recalledAt(b.getRecalledAt())
                .build();
    }
}
