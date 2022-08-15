package com.bbc.km.service;

import com.bbc.km.model.ItemStatus;
import com.bbc.km.model.Stats;
import com.bbc.km.repository.StatsRepository;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class StatsService {

    private final static LocalTime midnight = LocalTime.MIDNIGHT;
    private final static LocalDate today = LocalDate.now(ZoneId.systemDefault());

    public final static LocalDateTime todayMidnight = LocalDateTime.of(today, midnight);
    public final static LocalDateTime tomorrowMidnight = todayMidnight.plusDays(1);
    public final static PageRequest singlePage = PageRequest.of(0, 1);

    private final StatsRepository statsRepository;

    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public List<Stats> get(String from, String to) throws ParseException {
        if (from == null || Strings.isBlank(from))
            throw new NullPointerException("Starting date must have a value!");

        Date fromDate = new SimpleDateFormat("dd/MM/yyyy").parse(from);

        if (to == null) {
            Calendar fromCalendar = Calendar.getInstance();
            fromCalendar.setTime(fromDate);

            Calendar todayCalendar = Calendar.getInstance();
            todayCalendar.setTime(new Date());

            if (todayCalendar.get(Calendar.YEAR) == fromCalendar.get(Calendar.YEAR)
                    && todayCalendar.get(Calendar.MONTH) == fromCalendar.get(Calendar.MONTH)
                    && todayCalendar.get(Calendar.DAY_OF_MONTH) == fromCalendar.get(Calendar.DAY_OF_MONTH)) {
                return today();
            } else {
                LocalDateTime selectedFromMidnight = LocalDateTime.of(fromDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate(), midnight);
                LocalDateTime tomorrowSelectedFromMidnight = selectedFromMidnight.plusDays(1);
                return statsRepository.findByDateRange(selectedFromMidnight, tomorrowSelectedFromMidnight, singlePage);
            }
        } else {
            Date toDate = new SimpleDateFormat("dd/MM/yyyy").parse(to);

            LocalDateTime selectedFromMidnight = LocalDateTime.of(fromDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(), midnight);
            LocalDateTime tomorrowSelectedFromMidnight = LocalDateTime.of(toDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(), midnight).plusDays(1);
            return statsRepository.findByDateRange(selectedFromMidnight, tomorrowSelectedFromMidnight, singlePage);
        }
    }

    public List<Stats> today() {
        return statsRepository.findByDateRange(todayMidnight, tomorrowMidnight, singlePage);
    }

    public boolean existToday() {
        return !today().isEmpty();
    }

    public Stats create() {
        return statsRepository.save(new Stats(0));
    }

    public void update(ItemStatus previous, ItemStatus now) {
        Stats stats = statsRepository.findByDateRange(todayMidnight, tomorrowMidnight, singlePage).get(0);
        Map<ItemStatus, Integer> map = stats.getStatusCount();
        if (previous != null)
            map.put(previous, map.get(previous) - 1);
        else
            stats.addCount();
        map.put(now, map.get(now) + 1);
        statsRepository.save(stats);
    }
}
