package com.dku.council.domain.ticket.controller;

import com.dku.council.domain.ticket.model.dto.TicketEventDto;
import com.dku.council.domain.ticket.model.dto.request.RequestEnrollDto;
import com.dku.council.domain.ticket.model.dto.request.RequestNewTicketEventDto;
import com.dku.council.domain.ticket.model.dto.response.ResponseCaptchaKeyDto;
import com.dku.council.domain.ticket.model.dto.response.ResponseTicketDto;
import com.dku.council.domain.ticket.service.TicketEventService;
import com.dku.council.domain.ticket.service.TicketService;
import com.dku.council.global.auth.jwt.AppAuthentication;
import com.dku.council.global.auth.role.AdminAuth;
import com.dku.council.global.auth.role.UserAuth;
import com.dku.council.global.model.dto.ResponseIdDto;
import com.dku.council.infra.naver.service.CaptchaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.List;

@Tag(name = "티켓", description = "티켓 관련 API")
@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketEventService ticketEventService;
    private final CaptchaService captchaService;

    /**
     * 티켓 이벤트 목록 가져오기
     * <p>등록된 티켓 이벤트 목록을 가져옵니다.</p>
     *
     * @return 티켓 이벤트 목록
     */
    @GetMapping("/event")
    public List<TicketEventDto> list() {
        return ticketEventService.list();
    }

    /**
     * 티켓 이벤트 등록하기
     *
     * @param dto 티켓 이벤트 정보
     */
    @PostMapping("/event")
    @AdminAuth
    public ResponseIdDto newTicketEvent(@Valid @RequestBody RequestNewTicketEventDto dto) {
        Long id = ticketEventService.newTicketEvent(dto);
        return new ResponseIdDto(id);
    }

    /**
     * 티켓 이벤트 삭제하기
     * <p>티켓 이벤트를 삭제합니다. 이벤트에 등록된 사용자들의 티켓도 함께 삭제됩니다.</p>
     *
     * @param ticketEventId 티켓 이벤트 아이디
     */
    @DeleteMapping("/event/{ticketEventId}")
    @AdminAuth
    public void deleteTicketEvent(@PathVariable Long ticketEventId) {
        ticketEventService.deleteTicketEvent(ticketEventId);
    }

    /**
     * 내 티켓 보기
     * <p>내가 신청한 특정 이벤트의 티켓을 보여줍니다. 예비 번호가 포함되어있습니다.</p>
     *
     * @param ticketEventId 티켓 이벤트 아이디
     * @return 티켓 정보
     */
    @GetMapping("/{ticketEventId}")
    @UserAuth
    public ResponseTicketDto myTicket(AppAuthentication auth, @PathVariable Long ticketEventId) {
        return ticketService.myTicket(auth.getUserId(), ticketEventId);
    }

    /**
     * captcha 인증 키 요청
     * <p>새로운 Captcha 키를 요청합니다.</p>
     *
     * @return Captcha 키
     */
    @GetMapping("/captcha/key")
    @UserAuth
    public ResponseCaptchaKeyDto captchaKey() {
        String captchaKey = captchaService.requestCaptchaKey();
        return new ResponseCaptchaKeyDto(captchaKey);
    }

    /**
     * captcha 이미지 요청
     * <p>새로운 Captcha 이미지를 요청합니다.</p>
     * <p>하나의 키로 여러 번 이미지를 요청할 수 있습니다. 요청마다 다른 이미지가 전달됩니다.</p>
     *
     * @return Captcha JPEG 이미지 binary
     */
    @GetMapping(value = "/captcha/image/{key}",
            produces = MediaType.IMAGE_JPEG_VALUE)
    @UserAuth
    public byte[] captchaImage(@PathVariable String key) {
        return captchaService.requestCaptchaImage(key);
    }

    /**
     * 티켓 신청하기
     * <p>티켓 이벤트에 신청합니다. Captcha는 인증 실패시 키부터 다시 요청해야합니다.</p>
     *
     * @param dto 티켓 신청 정보
     */
    @PostMapping
    @UserAuth
    public ResponseTicketDto enroll(AppAuthentication auth,
                                    @Valid @RequestBody RequestEnrollDto dto) {
        Instant now = Instant.now();
        captchaService.verifyCaptcha(dto.getCaptchaKey(), dto.getCaptchaValue());
        return ticketService.enroll(auth.getUserId(), dto.getEventId(), now);
    }
}