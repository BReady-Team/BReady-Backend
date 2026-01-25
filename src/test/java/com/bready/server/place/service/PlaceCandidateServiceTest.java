package com.bready.server.place.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.Place;
import com.bready.server.place.domain.PlaceCandidate;
import com.bready.server.place.dto.PlaceCandidateCreateRequest;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.repository.PlaceCandidateRepository;
import com.bready.server.plan.domain.CategoryState;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.repository.CategorySelectionLogRepository;
import com.bready.server.plan.repository.CategoryStateRepository;
import com.bready.server.plan.repository.PlanCategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PlaceCandidateServiceTest {

    @InjectMocks
    private PlaceCandidateService placeCandidateService;

    @Mock
    private PlanCategoryRepository planCategoryRepository;

    @Mock
    private PlaceCandidateRepository placeCandidateRepository;

    @Mock
    private PlacePersistenceService placePersistenceService;

    @Mock
    private CategoryStateRepository categoryStateRepository;

    @Mock
    private CategorySelectionLogRepository categorySelectionLogRepository;

    @Test
    @DisplayName("장소 후보 등록 성공")
    void createCandidate_success() {
        PlaceCandidateCreateRequest request =
                new PlaceCandidateCreateRequest(
                        1L,
                        10L,
                        "kakao-123",
                        "성수 카페",
                        "서울 성동구",
                        BigDecimal.valueOf(37.5),
                        BigDecimal.valueOf(127.0),
                        true
                );

        PlanCategory category = mock(PlanCategory.class);
        Place place = mock(Place.class);

        PlaceCandidate savedCandidate = mock(PlaceCandidate.class);
        when(savedCandidate.getId()).thenReturn(100L);
        when(savedCandidate.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(planCategoryRepository.findByIdAndPlan_Id(10L, 1L))
                .thenReturn(Optional.of(category));

        when(placePersistenceService.getOrCreate(request))
                .thenReturn(place);

        when(placeCandidateRepository.saveAndFlush(any()))
                .thenReturn(savedCandidate);

        var response = placeCandidateService.createCandidate(request);

        assertThat(response.candidateId()).isEqualTo(100L);
        verify(placeCandidateRepository).saveAndFlush(any());
    }

    @Test
    @DisplayName("장소 후보 중복 등록 → DUPLICATE_PLACE_CANDIDATE (실패)")
    void createCandidate_duplicate() {
        PlaceCandidateCreateRequest request =
                new PlaceCandidateCreateRequest(
                        1L,
                        10L,
                        "kakao-123",
                        "성수 카페",
                        "서울 성동구",
                        BigDecimal.valueOf(37.5),
                        BigDecimal.valueOf(127.0),
                        true
                );

        PlanCategory category = mock(PlanCategory.class);
        Place place = mock(Place.class);

        when(planCategoryRepository.findByIdAndPlan_Id(10L, 1L))
                .thenReturn(Optional.of(category));

        when(placePersistenceService.getOrCreate(request))
                .thenReturn(place);

        when(placeCandidateRepository.saveAndFlush(any()))
                .thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
                placeCandidateService.createCandidate(request)
        )
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCase")
                .isEqualTo(PlaceErrorCase.DUPLICATE_PLACE_CANDIDATE);
    }

    @Test
    @DisplayName("대표 장소 후보 최초 선택 성공")
    void setRepresentative_firstTime() {
        Long candidateId = 100L;

        PlanCategory category = mock(PlanCategory.class);
        when(category.getId()).thenReturn(10L);

        PlaceCandidate candidate = mock(PlaceCandidate.class);
        when(candidate.getCategory()).thenReturn(category);

        when(placeCandidateRepository.findByIdWithCategoryAndPlace(candidateId))
                .thenReturn(Optional.of(candidate));

        when(categoryStateRepository.findByCategory_IdForUpdate(10L))
                .thenReturn(Optional.empty());

        var response = placeCandidateService.setRepresentative(candidateId);

        assertThat(response.representativeCandidateId()).isEqualTo(candidateId);
        verify(categoryStateRepository).save(any());
        verify(categorySelectionLogRepository).save(any());
    }

    @Test
    @DisplayName("이미 대표 후보 → 예외 (실패)")
    void setRepresentative_alreadyRepresentative() {
        Long candidateId = 100L;

        PlanCategory category = mock(PlanCategory.class);
        when(category.getId()).thenReturn(10L);

        PlaceCandidate candidate = mock(PlaceCandidate.class);
        when(candidate.getCategory()).thenReturn(category);

        CategoryState state = mock(CategoryState.class);
        when(state.isRepresentative(candidateId)).thenReturn(true);

        when(placeCandidateRepository.findByIdWithCategoryAndPlace(candidateId))
                .thenReturn(Optional.of(candidate));

        when(categoryStateRepository.findByCategory_IdForUpdate(10L))
                .thenReturn(Optional.of(state));

        assertThatThrownBy(() ->
                placeCandidateService.setRepresentative(candidateId)
        )
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCase")
                .isEqualTo(PlaceErrorCase.ALREADY_REPRESENTATIVE_CANDIDATE);
    }

    @Test
    @DisplayName("장소 후보 삭제 성공")
    void deleteCandidate_success() {
        Long candidateId = 100L;

        PlanCategory category = mock(PlanCategory.class);
        when(category.getId()).thenReturn(10L);

        PlaceCandidate candidate = mock(PlaceCandidate.class);
        when(candidate.getCategory()).thenReturn(category);

        when(placeCandidateRepository.findByIdWithCategory(candidateId))
                .thenReturn(Optional.of(candidate));

        when(categoryStateRepository.findByCategory_IdForUpdate(10L))
                .thenReturn(Optional.empty());

        var response = placeCandidateService.deleteCandidate(candidateId);

        assertThat(response.candidateId()).isEqualTo(candidateId);
        verify(candidate).softDelete();
    }
}