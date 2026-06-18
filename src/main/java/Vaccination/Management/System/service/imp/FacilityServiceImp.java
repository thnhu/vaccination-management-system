package Vaccination.Management.System.service.imp;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.facility.CreateFacilityRequest;
import Vaccination.Management.System.model.dto.facility.FacilityCapacityRequest;
import Vaccination.Management.System.model.dto.facility.FacilityCapacityResponse;
import Vaccination.Management.System.model.dto.facility.FacilityResponse;
import Vaccination.Management.System.model.dto.facility.UpdateFacilityRequest;
import Vaccination.Management.System.model.entity.AdministrativeUnit;
import Vaccination.Management.System.model.entity.Facility;
import Vaccination.Management.System.model.entity.FacilityCapacity;
import Vaccination.Management.System.repository.AdministrativeUnitRepository;
import Vaccination.Management.System.repository.FacilityCapacityRepository;
import Vaccination.Management.System.repository.FacilityRepository;
import Vaccination.Management.System.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityServiceImp implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityCapacityRepository facilityCapacityRepository;
    private final AdministrativeUnitRepository administrativeUnitRepository;

    @Override
    @Transactional
    public FacilityResponse createFacility(CreateFacilityRequest request) {
        AdministrativeUnit province = administrativeUnitRepository.findById(request.getProvinceCode())
                .orElseThrow(() -> new AppException(ErrorCode.ADMINISTRATIVE_UNIT_NOT_FOUND));

        AdministrativeUnit ward = null;
        if (request.getWardCode() != null) {
            ward = administrativeUnitRepository.findById(request.getWardCode())
                    .orElseThrow(() -> new AppException(ErrorCode.ADMINISTRATIVE_UNIT_NOT_FOUND));

            if (ward.getParent() == null || !ward.getParent().getCode().equals(province.getCode())) {
                throw new AppException(ErrorCode.FACILITY_WARD_PROVINCE_MISMATCH);
            }
        }

        Facility facility = Facility.builder()
                .name(request.getName())
                .facilityType(request.getFacilityType())
                .address(request.getAddress())
                .province(province)
                .ward(ward)
                .phone(request.getPhone())
                .build();

        facility = facilityRepository.save(facility);

        FacilityCapacity capacity = FacilityCapacity.builder()
                .facility(facility)
                .maxSlotsPerDay(request.getMaxSlotsPerDay())
                .effectiveFrom(request.getCapacityEffectiveFrom())
                .build();

        facilityCapacityRepository.save(capacity);

        return toResponse(facility);
    }

    @Override
    public FacilityResponse getFacilityById(Long id) {
        return facilityRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_NOT_FOUND));
    }

    @Override
    public List<FacilityResponse> getAllFacilities() {
        return facilityRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FacilityResponse updateFacility(Long id, UpdateFacilityRequest request) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_NOT_FOUND));

        if (request.getName() != null) {
            facility.setName(request.getName());
        }
        if (request.getFacilityType() != null) {
            facility.setFacilityType(request.getFacilityType());
        }
        if (request.getAddress() != null) {
            facility.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            facility.setPhone(request.getPhone());
        }
        if (request.getProvinceCode() != null) {
            AdministrativeUnit province = administrativeUnitRepository.findById(request.getProvinceCode())
                    .orElseThrow(() -> new AppException(ErrorCode.ADMINISTRATIVE_UNIT_NOT_FOUND));
            facility.setProvince(province);
        }
        if (request.getWardCode() != null) {
            AdministrativeUnit ward = administrativeUnitRepository.findById(request.getWardCode())
                    .orElseThrow(() -> new AppException(ErrorCode.ADMINISTRATIVE_UNIT_NOT_FOUND));
            AdministrativeUnit currentProvince = facility.getProvince();
            if (ward.getParent() == null || !ward.getParent().getCode().equals(currentProvince.getCode())) {
                throw new AppException(ErrorCode.FACILITY_WARD_PROVINCE_MISMATCH);
            }
            facility.setWard(ward);
        }

        facility = facilityRepository.save(facility);
        return toResponse(facility);
    }

    @Override
    @Transactional
    public FacilityResponse deactivateFacility(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_NOT_FOUND));

        if (!facility.isActive()) {
            throw new AppException(ErrorCode.FACILITY_ALREADY_INACTIVE);
        }

        facility.setActive(false);
        facility = facilityRepository.save(facility);
        return toResponse(facility);
    }

    @Override
    @Transactional
    public FacilityCapacityResponse createCapacity(Long facilityId, FacilityCapacityRequest request) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_NOT_FOUND));

        FacilityCapacity capacity = FacilityCapacity.builder()
                .facility(facility)
                .maxSlotsPerDay(request.getMaxSlotsPerDay())
                .effectiveFrom(request.getEffectiveFrom())
                .build();

        capacity = facilityCapacityRepository.save(capacity);
        return toCapacityResponse(capacity);
    }

    @Override
    public FacilityCapacityResponse getCapacity(Long facilityId) {
        facilityRepository.findById(facilityId)
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_NOT_FOUND));

        FacilityCapacity capacity = facilityCapacityRepository
                .findActiveCapacity(facilityId, LocalDate.now())
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_CAPACITY_NOT_FOUND));

        return toCapacityResponse(capacity);
    }

    private FacilityResponse toResponse(Facility f) {
        return FacilityResponse.builder()
                .id(f.getId())
                .name(f.getName())
                .facilityType(f.getFacilityType())
                .address(f.getAddress())
                .provinceCode(f.getProvince().getCode())
                .provinceName(f.getProvince().getName())
                .wardCode(f.getWard() != null ? f.getWard().getCode() : null)
                .wardName(f.getWard() != null ? f.getWard().getName() : null)
                .phone(f.getPhone())
                .active(f.isActive())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    private FacilityCapacityResponse toCapacityResponse(FacilityCapacity c) {
        return FacilityCapacityResponse.builder()
                .id(c.getId())
                .facilityId(c.getFacility().getId())
                .maxSlotsPerDay(c.getMaxSlotsPerDay())
                .effectiveFrom(c.getEffectiveFrom())
                .build();
    }
}
