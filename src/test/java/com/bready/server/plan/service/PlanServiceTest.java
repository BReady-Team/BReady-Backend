package com.bready.server.plan.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.dto.*;
import com.bready.server.plan.exception.PlanErrorCase;
import com.bready.server.plan.repository.CategoryStateRepository;
import com.bready.server.plan.repository.PlanCategoryRepository;
import com.bready.server.plan.repository.PlanRepository;
import com.bready.server.user.domain.User;
import com.bready.server.user.domain.UserProfile;
import com.bready.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PlanCategoryRepository planCategoryRepository;

    @Mock
    private CategoryStateRepository categoryStateRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlanService planService;

    private final Long USER_ID = 1L;
    private final Long OTHER_USER_ID = 2L;

    @Nested
    @DisplayName("플랜 생성")
    class CreatePlan {

        @Test
        @DisplayName("성공")
        void success() {
            Plan plan = Plan.create(USER_ID, "제목", LocalDate.now(), "서울");

            given(planRepository.save(any()))
                    .willReturn(plan);

            PlanCreateRequest request = new PlanCreateRequest("제목", LocalDate.now(), "서울");

            PlanCreateResponse response = planService.createPlan(USER_ID, request);

            assertThat(response.getPlanId()).isEqualTo(plan.getId());
        }
    }

    @Nested
    @DisplayName("플랜 수정")
    class UpdatePlan {

        @Test
        @DisplayName("성공")
        void success() {
            Plan plan = Plan.create(USER_ID, "old", LocalDate.now(), "서울");

            given(planRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(plan));

            PlanUpdateRequest request = new PlanUpdateRequest("new", LocalDate.now(), "부산");

            PlanUpdateResponse response = planService.updatePlan(USER_ID, 1L, request);

            assertThat(response.getPlanId()).isEqualTo(plan.getId());
            assertThat(plan.getTitle()).isEqualTo("new");
        }

        @Test
        @DisplayName("플랜 없음 → 404")
        void notFound() {
            given(planRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    planService.updatePlan(USER_ID, 1L, new PlanUpdateRequest("t", LocalDate.now(), "서울"))
            ).isInstanceOf(ApplicationException.class)
                    .extracting("errorCode")
                    .isEqualTo(PlanErrorCase.PLAN_NOT_FOUND.getErrorCode());
        }

        @Test
        @DisplayName("권한 없음 → 403")
        void accessDenied() {
            Plan plan = Plan.create(OTHER_USER_ID, "title", LocalDate.now(), "서울");

            given(planRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(plan));

            assertThatThrownBy(() ->
                    planService.updatePlan(USER_ID, 1L, new PlanUpdateRequest("t", LocalDate.now(), "서울"))
            ).isInstanceOf(ApplicationException.class)
                    .extracting("errorCode")
                    .isEqualTo(PlanErrorCase.PLAN_ACCESS_DENIED.getErrorCode());
        }
    }

    @Nested
    @DisplayName("플랜 상세 조회")
    class GetPlanDetail {

        @Test
        @DisplayName("플랜 없음 → 404")
        void notFound() {
            given(planRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    planService.getPlanDetail(USER_ID, 1L)
            ).isInstanceOf(ApplicationException.class)
                    .extracting("errorCode")
                    .isEqualTo(PlanErrorCase.PLAN_NOT_FOUND.getErrorCode());
        }

        @Test
        @DisplayName("권한 없음 → 403")
        void accessDenied() {
            Plan plan = Plan.create(OTHER_USER_ID, "title", LocalDate.now(), "서울");

            given(planRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(plan));

            assertThatThrownBy(() ->
                    planService.getPlanDetail(USER_ID, 1L)
            ).isInstanceOf(ApplicationException.class)
                    .extracting("errorCode")
                    .isEqualTo(PlanErrorCase.PLAN_ACCESS_DENIED.getErrorCode());
        }

        @Test
        @DisplayName("카테고리 없음 → 빈 리스트 반환")
        void emptyCategories() {
            Plan plan = Plan.create(USER_ID, "title", LocalDate.now(), "서울");

            User user = User.createLocal("test@test.com", "encoded-password");

            UserProfile profile = UserProfile.create(user, "테스터");

            profile.changeProfileImage(null);

            given(userRepository.findByIdWithProfile(USER_ID))
                    .willReturn(Optional.of(user));

            given(planRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(plan));

            given(planCategoryRepository.findAllDetailByPlanId(1L))
                    .willReturn(List.of());

            PlanDetailResponse response = planService.getPlanDetail(USER_ID, 1L);

            assertThat(response.getCategories()).isEmpty();
        }
    }

    @Nested
    @DisplayName("플랜 조회")
    class GetMyPlans {

        @Test
        @DisplayName("성공")
        void success() {
            Plan plan = Plan.create(USER_ID, "title", LocalDate.now(), "서울");

            Page<Plan> page = new PageImpl<>(List.of(plan));

            given(planRepository.findAllByOwnerIdAndDeletedAtIsNull(eq(USER_ID), any(Pageable.class)))
                    .willReturn(page);

            PlanListResponse response = planService.getMyPlans(USER_ID, 0, 10, SortDirection.DESC);

            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getPlanId()).isEqualTo(plan.getId());
        }
    }

    @Nested
    @DisplayName("플랜 삭제")
    class DeletePlan {

        @Test
        @DisplayName("성공 (soft delete)")
        void success() {
            Plan plan = Plan.create(USER_ID, "title", LocalDate.now(), "서울");

            given(planRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(plan));

            PlanDeleteResponse response = planService.deletePlan(USER_ID, 1L);

            assertThat(response.getPlanId()).isEqualTo(plan.getId());
            assertThat(plan.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("플랜 없음 → 404")
        void notFound() {
            given(planRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    planService.deletePlan(USER_ID, 1L)
            ).isInstanceOf(ApplicationException.class)
                    .extracting("errorCode")
                    .isEqualTo(PlanErrorCase.PLAN_NOT_FOUND.getErrorCode());
        }

        @Test
        @DisplayName("권한 없음 → 403")
        void accessDenied() {
            Plan plan = Plan.create(OTHER_USER_ID, "title", LocalDate.now(), "서울");

            given(planRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(plan));

            assertThatThrownBy(() ->
                    planService.deletePlan(USER_ID, 1L)
            ).isInstanceOf(ApplicationException.class)
                    .extracting("errorCode")
                    .isEqualTo(PlanErrorCase.PLAN_ACCESS_DENIED.getErrorCode());
        }
    }
}