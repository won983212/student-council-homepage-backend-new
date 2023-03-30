package com.dku.council.domain.post.service;

import com.dku.council.domain.comment.service.CommentService;
import com.dku.council.domain.like.model.LikeTarget;
import com.dku.council.domain.like.service.impl.CachedLikeServiceImpl;
import com.dku.council.domain.post.model.dto.response.ResponsePetitionDto;
import com.dku.council.domain.post.model.entity.posttype.Petition;
import com.dku.council.domain.post.repository.GenericPostRepository;
import com.dku.council.domain.statistic.model.dto.PetitionStatisticDto;
import com.dku.council.domain.statistic.service.PetitionStatisticService;
import com.dku.council.domain.tag.service.TagService;
import com.dku.council.domain.user.repository.UserRepository;
import com.dku.council.infra.nhn.service.FileUploadService;
import com.dku.council.mock.PetitionMock;
import com.dku.council.mock.PetitionStatisticMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PetitionServiceTest {
    @Mock
    private GenericPostRepository<Petition> petitionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagService tagService;

    @Mock
    private ViewCountService viewCountService;

    @Mock
    private FileUploadService fileUploadService;

    @Mock
    private CachedLikeServiceImpl postLikeService;

    @Mock
    private PetitionStatisticService petitionStatisticService;
    @Mock
    private CommentService commentService;

    private PetitionService petitionService;


    @BeforeEach
    public void setup() {
        GenericPostService<Petition> postService = new GenericPostService<>(petitionRepository, userRepository, tagService,
                viewCountService, fileUploadService, postLikeService);
        petitionService = new PetitionService(postService, commentService, petitionStatisticService, 150, Duration.ofDays(30));
    }

    @Test
    @DisplayName("Petition mapper 와 함께 단건 조회가 잘 동작하는지?")
    public void findOnePetitionWithMapper() {
        // given
        Petition petition = PetitionMock.createWithDummy();
        List<PetitionStatisticDto> list = PetitionStatisticMock.createList();

        when(petitionRepository.findById(petition.getId())).thenReturn(Optional.of(petition));
        when(postLikeService.isLiked(any(), any(), eq(LikeTarget.POST))).thenReturn(true);
        when(petitionStatisticService.findTop4Department(petition.getId())).thenReturn(list);
        //when
        ResponsePetitionDto dto = petitionService.findOnePetition(petition.getId(), 0L, "Addr");

        // then
        assertThat(dto.getId()).isEqualTo(petition.getId());
        assertThat(dto.getViews()).isEqualTo(petition.getViews());
        assertThat(dto.getAnswer()).isEqualTo(petition.getAnswer());
        assertThat(dto.isLiked()).isEqualTo(true);
        assertThat(dto.isMine()).isEqualTo(false);
        assertThat(dto.getExpiresAt()).isEqualTo(petition.getCreatedAt().plusDays(30).toLocalDate());
        assertThat(dto.getStatisticList()).isEqualTo(list);
    }

}