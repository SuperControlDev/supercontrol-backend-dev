package com.supercontrol.backend.scheduler;

import com.supercontrol.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketScheduler {

    private final UserRepository userRepository;

    /**
     * 🇰🇷 한국(KST) 기준 매일 00:00에 실행
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    // @Scheduled(fixedRate = 10000) // 테스트용 (주기 : 10초)
    public void distributeKorea() {
        System.out.println("🎟 [KOREA] 무료 티켓 지급 시작!");
        userRepository.updateDailyFreeTicketsKorea();
        System.out.println("🎟 [KOREA] 무료 티켓 지급 완료!");
    }

    /**
     * 🇨🇳 중국 기준 매일 00:00에 실행 (Asia/Shanghai)
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Shanghai")
    public void distributeChina() {
        System.out.println("🎟 [CHINA] 무료 티켓 지급 시작!");
        userRepository.updateDailyFreeTicketsChina();
        System.out.println("🎟 [CHINA] 무료 티켓 지급 완료!");
    }

    /**
     * 🇯🇵 일본 기준 매일 00:00에 실행 (Asia/Tokyo)
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Tokyo")
    public void distributeJapan() {
        System.out.println("🎟 [JAPAN] 무료 티켓 지급 시작!");
        userRepository.updateDailyFreeTicketsJapan();
        System.out.println("🎟 [JAPAN] 무료 티켓 지급 완료!");
    }

    /**
     * 🇺🇸 미국 서부(PST) 기준 매일 00:00에 실행 (America/Los_Angeles)
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Los_Angeles")
    public void distributeUSWest() {
        System.out.println("🎟 [USA-West] 무료 티켓 지급 시작!");
        userRepository.updateDailyFreeTicketsUSA();
        System.out.println("🎟 [USA-West] 무료 티켓 지급 완료!");
    }

    /**
     * 🇺🇸 미국 동부(EST) 기준 매일 00:00에 실행 (America/New_York)
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "America/New_York")
    public void distributeUSEast() {
        System.out.println("🎟 [USA-East] 무료 티켓 지급 시작!");
        userRepository.updateDailyFreeTicketsUSA();
        System.out.println("🎟 [USA-East] 무료 티켓 지급 완료!");
    }
}
