package com.supercontrol.backend.repository;

import com.supercontrol.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 🇰🇷 한국 사용자 대상 무료 티켓 지급 배치
     * free_tickets = min(free_tickets + 5, 5)
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.freeTickets = LEAST(u.freeTickets + 5, 5) WHERE u.countryCode = 'KR'")
    void updateDailyFreeTicketsKorea();

    /**
     * 🇨🇳 중국 사용자 대상 무료 티켓 지급 배치
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.freeTickets = LEAST(u.freeTickets + 5, 5) WHERE u.countryCode = 'CN'")
    void updateDailyFreeTicketsChina();

    /**
     * 🇯🇵 일본 사용자 대상 무료 티켓 지급 배치
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.freeTickets = LEAST(u.freeTickets + 5, 5) WHERE u.countryCode = 'JP'")
    void updateDailyFreeTicketsJapan();

    /**
     * 🇺🇸 미국 사용자 대상 무료 티켓 지급 배치
     * (필요시 East/West 나누거나 countryCode = 'US' 로 통일 가능)
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.freeTickets = LEAST(u.freeTickets + 5, 5) WHERE u.countryCode = 'US'")
    void updateDailyFreeTicketsUSA();

    /**
     * 특정 국가 코드로 배치 실행하는 확장 버전 (향후 필요 시 사용 가능)
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.freeTickets = LEAST(u.freeTickets + 5, 5) WHERE u.countryCode = ?1")
    void updateDailyFreeTicketsByCountry(String countryCode);

}
