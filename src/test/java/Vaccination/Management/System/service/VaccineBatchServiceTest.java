package Vaccination.Management.System.service;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.vaccinebatch.CreateVaccineBatchRequest;
import Vaccination.Management.System.model.dto.vaccinebatch.VaccineBatchResponse;
import Vaccination.Management.System.model.entity.Facility;
import Vaccination.Management.System.model.entity.Manufacturer;
import Vaccination.Management.System.model.entity.Supplier;
import Vaccination.Management.System.model.entity.User;
import Vaccination.Management.System.model.entity.Vaccine;
import Vaccination.Management.System.model.entity.VaccineBatch;
import Vaccination.Management.System.model.enums.BatchStatus;
import Vaccination.Management.System.model.enums.FacilityType;
import Vaccination.Management.System.model.enums.UserRole;
import Vaccination.Management.System.model.enums.VaccineCategory;
import Vaccination.Management.System.repository.FacilityRepository;
import Vaccination.Management.System.repository.ManufacturerRepository;
import Vaccination.Management.System.repository.SupplierRepository;
import Vaccination.Management.System.repository.UserRepository;
import Vaccination.Management.System.repository.VaccineBatchRepository;
import Vaccination.Management.System.repository.VaccineRepository;
import Vaccination.Management.System.service.imp.VaccineBatchServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaccineBatchServiceTest {

    @Mock private VaccineBatchRepository vaccineBatchRepository;
    @Mock private FacilityRepository facilityRepository;
    @Mock private VaccineRepository vaccineRepository;
    @Mock private UserRepository userRepository;
    @Mock private ManufacturerRepository manufacturerRepository;
    @Mock private SupplierRepository supplierRepository;

    @InjectMocks private VaccineBatchServiceImp vaccineBatchService;

    private Facility activeFacility;
    private Vaccine activeVaccine;
    private User staff;

    @BeforeEach
    void setUp() {
        activeFacility = Facility.builder()
                .id(10L).name("Cơ sở A").active(true).facilityType(FacilityType.VACCINATION_CENTER).build();
        activeVaccine = Vaccine.builder()
                .id(20L).name("Vắc xin X").active(true).category(VaccineCategory.CHILD).build();
        staff = User.builder()
                .id(2L).fullName("Nhân viên B").role(UserRole.MEDICAL_STAFF).build();
    }

    // ── createBatch ───────────────────────────────────────────────────────────

    @Test
    void createBatch_success_withoutOptionalFields() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(activeFacility));
        when(vaccineRepository.findById(20L)).thenReturn(Optional.of(activeVaccine));
        when(vaccineBatchRepository.existsByBatchNumberAndFacilityId("B001", 10L)).thenReturn(false);

        VaccineBatch saved = buildBatch(1L, "B001", 100, 100, BatchStatus.ACTIVE, null, null);
        when(vaccineBatchRepository.save(any(VaccineBatch.class))).thenReturn(saved);

        VaccineBatchResponse response = vaccineBatchService.createBatch(10L, buildRequest("B001", 100, null, null), 2L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBatchNumber()).isEqualTo("B001");
        assertThat(response.getQuantity()).isEqualTo(100);
        assertThat(response.getRemaining()).isEqualTo(100);
        assertThat(response.getStatus()).isEqualTo(BatchStatus.ACTIVE);
        assertThat(response.getVaccineId()).isEqualTo(20L);
        assertThat(response.getFacilityId()).isEqualTo(10L);
        assertThat(response.getManufacturerName()).isNull();
        assertThat(response.getSupplierName()).isNull();
        verify(vaccineBatchRepository).save(any(VaccineBatch.class));
    }

    @Test
    void createBatch_success_withManufacturerAndSupplier() {
        Manufacturer manufacturer = Manufacturer.builder().id(5L).name("Pfizer").build();
        Supplier supplier = Supplier.builder().id(6L).name("Nhà cung cấp A").build();

        when(facilityRepository.findById(10L)).thenReturn(Optional.of(activeFacility));
        when(vaccineRepository.findById(20L)).thenReturn(Optional.of(activeVaccine));
        when(vaccineBatchRepository.existsByBatchNumberAndFacilityId("B002", 10L)).thenReturn(false);
        when(manufacturerRepository.findById(5L)).thenReturn(Optional.of(manufacturer));
        when(supplierRepository.findById(6L)).thenReturn(Optional.of(supplier));

        VaccineBatch saved = buildBatch(2L, "B002", 50, 50, BatchStatus.ACTIVE, manufacturer, supplier);
        when(vaccineBatchRepository.save(any(VaccineBatch.class))).thenReturn(saved);

        CreateVaccineBatchRequest req = buildRequest("B002", 50, 5L, 6L);
        VaccineBatchResponse response = vaccineBatchService.createBatch(10L, req, 2L);

        assertThat(response.getManufacturerName()).isEqualTo("Pfizer");
        assertThat(response.getSupplierName()).isEqualTo("Nhà cung cấp A");
    }

    @Test
    void createBatch_throwsException_whenFacilityNotFound() {
        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineBatchService.createBatch(99L, buildRequest("B003", 10, null, null), 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_NOT_FOUND));
    }

    @Test
    void createBatch_throwsException_whenFacilityInactive() {
        Facility inactive = Facility.builder().id(11L).name("Cơ sở Inactive").active(false).build();
        when(facilityRepository.findById(11L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> vaccineBatchService.createBatch(11L, buildRequest("B004", 10, null, null), 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_INACTIVE));
    }

    @Test
    void createBatch_throwsException_whenVaccineNotFound() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(activeFacility));

        CreateVaccineBatchRequest req = buildRequest("B005", 10, null, null);
        req.setVaccineId(999L);
        when(vaccineRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineBatchService.createBatch(10L, req, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINE_NOT_FOUND));
    }

    @Test
    void createBatch_throwsException_whenVaccineInactive() {
        Vaccine inactive = Vaccine.builder().id(21L).name("Vắc xin Inactive").active(false).build();
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(activeFacility));

        CreateVaccineBatchRequest req = buildRequest("B006", 10, null, null);
        req.setVaccineId(21L);
        when(vaccineRepository.findById(21L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> vaccineBatchService.createBatch(10L, req, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINE_INACTIVE));
    }

    @Test
    void createBatch_throwsException_whenDuplicateBatchNumber() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(activeFacility));
        when(vaccineRepository.findById(20L)).thenReturn(Optional.of(activeVaccine));
        when(vaccineBatchRepository.existsByBatchNumberAndFacilityId("B007", 10L)).thenReturn(true);

        assertThatThrownBy(() -> vaccineBatchService.createBatch(10L, buildRequest("B007", 10, null, null), 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BATCH_DUPLICATE));
    }

    @Test
    void createBatch_throwsException_whenExpiryDateNotInFuture() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(activeFacility));
        when(vaccineRepository.findById(20L)).thenReturn(Optional.of(activeVaccine));
        when(vaccineBatchRepository.existsByBatchNumberAndFacilityId("B008", 10L)).thenReturn(false);

        CreateVaccineBatchRequest req = buildRequest("B008", 10, null, null);
        req.setExpiryDate(LocalDate.now()); // today is not "after" today

        assertThatThrownBy(() -> vaccineBatchService.createBatch(10L, req, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BATCH_UNAVAILABLE));
    }

    // ── getBatchesByFacility ──────────────────────────────────────────────────

    @Test
    void getBatchesByFacility_success() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(activeFacility));
        VaccineBatch batch = buildBatch(3L, "B009", 80, 60, BatchStatus.ACTIVE, null, null);
        when(vaccineBatchRepository.findByFacilityId(10L)).thenReturn(List.of(batch));

        List<VaccineBatchResponse> result = vaccineBatchService.getBatchesByFacility(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBatchNumber()).isEqualTo("B009");
        assertThat(result.get(0).getQuantity()).isEqualTo(80);
        assertThat(result.get(0).getRemaining()).isEqualTo(60);
    }

    @Test
    void getBatchesByFacility_returnsEmptyList_whenNoBatches() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(activeFacility));
        when(vaccineBatchRepository.findByFacilityId(10L)).thenReturn(List.of());

        List<VaccineBatchResponse> result = vaccineBatchService.getBatchesByFacility(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void getBatchesByFacility_throwsException_whenFacilityNotFound() {
        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineBatchService.getBatchesByFacility(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_NOT_FOUND));
    }

    // ── recallBatch ───────────────────────────────────────────────────────────

    @Test
    void recallBatch_success() {
        VaccineBatch active = buildBatch(4L, "B010", 100, 70, BatchStatus.ACTIVE, null, null);
        when(vaccineBatchRepository.findById(4L)).thenReturn(Optional.of(active));
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(vaccineBatchRepository.save(any(VaccineBatch.class))).thenAnswer(inv -> inv.getArgument(0));

        VaccineBatchResponse response = vaccineBatchService.recallBatch(4L, 2L);

        assertThat(response.getStatus()).isEqualTo(BatchStatus.RECALLED);
        assertThat(response.getRecalledAt()).isNotNull();
        verify(vaccineBatchRepository).save(any(VaccineBatch.class));
    }

    @Test
    void recallBatch_throwsException_whenBatchNotFound() {
        when(vaccineBatchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineBatchService.recallBatch(99L, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BATCH_NOT_FOUND));
    }

    @Test
    void recallBatch_throwsException_whenAlreadyRecalled() {
        VaccineBatch recalled = buildBatch(5L, "B011", 100, 50, BatchStatus.RECALLED, null, null);
        when(vaccineBatchRepository.findById(5L)).thenReturn(Optional.of(recalled));

        assertThatThrownBy(() -> vaccineBatchService.recallBatch(5L, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BATCH_ALREADY_RECALLED));
    }

    @Test
    void recallBatch_throwsException_whenStaffNotFound() {
        VaccineBatch active = buildBatch(6L, "B012", 100, 80, BatchStatus.ACTIVE, null, null);
        when(vaccineBatchRepository.findById(6L)).thenReturn(Optional.of(active));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineBatchService.recallBatch(6L, 99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CreateVaccineBatchRequest buildRequest(String batchNumber, int qty,
                                                    Long manufacturerId, Long supplierId) {
        CreateVaccineBatchRequest req = new CreateVaccineBatchRequest();
        req.setVaccineId(20L);
        req.setBatchNumber(batchNumber);
        req.setQuantity(qty);
        req.setExpiryDate(LocalDate.now().plusYears(1));
        req.setReceivedDate(LocalDate.now());
        req.setManufacturerId(manufacturerId);
        req.setSupplierId(supplierId);
        req.setPrice(BigDecimal.valueOf(150000));
        return req;
    }

    private VaccineBatch buildBatch(Long id, String batchNumber, int qty, int remaining,
                                    BatchStatus status, Manufacturer manufacturer, Supplier supplier) {
        return VaccineBatch.builder()
                .id(id)
                .vaccine(activeVaccine)
                .facility(activeFacility)
                .batchNumber(batchNumber)
                .quantity(qty)
                .remaining(remaining)
                .status(status)
                .expiryDate(LocalDate.now().plusYears(1))
                .receivedDate(LocalDate.now())
                .manufacturer(manufacturer)
                .supplier(supplier)
                .build();
    }
}
