package com.bbc.km.service;

import com.bbc.km.model.ItemStatus;
import com.bbc.km.model.Stats;
import com.bbc.km.repository.StatsRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.bbc.km.service.StatsService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({SpringExtension.class, MockitoExtension.class})
public class StatsServiceTest {

    private final Stats today;

    @Mock
    private StatsRepository statsRepository;
    private StatsService statsService;

    public StatsServiceTest() {
        this.today = new Stats(0);
    }

    @BeforeAll
    public void beforeAll() {
        Mockito.when(statsRepository.findByDateRange(any(), any(), eq(singlePage)))
                .thenAnswer(invocationOnMock -> {
                            LocalDateTime from = invocationOnMock.getArgument(0);
                            LocalDateTime to = invocationOnMock.getArgument(1);
                            if (from.equals(todayMidnight)
                                    || (from.equals(todayMidnight) && to.equals(tomorrowMidnight))) {
                                return List.of(today);
                            }
                            return List.of();
                        }
                );

        Mockito.when(statsRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

        this.statsService = new StatsService(statsRepository);
    }

    @AfterAll
    public void afterAll() {
        Mockito.reset(statsRepository);
    }

    @Test
    public void create_shouldInsert_todayStats() {
        Stats created = statsService.create();
        Stats today = statsService.today().get(0);

        assertEquals(created.getCount(), today.getCount());
    }

    @Test
    public void update_doneOrder_shouldIncrement_DoneCounter() {

        Stats today = statsService.today().get(0);

        assertEquals(today.getStatusCount().get(ItemStatus.DONE), 0);

        statsService.update(ItemStatus.PROGRESS, ItemStatus.DONE);

        assertEquals(today.getStatusCount().get(ItemStatus.DONE), 1);
    }

    @Test
    public void update_newOrderInsert_shouldIncrement_TodoCounterAndCounter() {

        Stats today = statsService.today().get(0);

        assertEquals(today.getCount(), 0);

        statsService.update(null, ItemStatus.TODO);

        assertEquals(today.getStatusCount().get(ItemStatus.TODO), 1);
        assertEquals(today.getCount(), 1);
    }

    @Test
    public void todayStats_whenServiceGet_shouldReturnToday() {
        assertThat(statsService.today().size()).isEqualTo(1);
    }

    @Test
    public void todayStats_whenServiceCheckExist_shouldReturnTrue() {
        assertThat(statsService.existToday()).isEqualTo(true);
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void fromBlank_shouldThrows_NullPointerException(String from) {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            statsService.get(from, null);
        });

        String expectedMessage = "Starting date must have a value!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @ParameterizedTest
    @ValueSource(strings = {"testString", "2022-12-08", "08122022"})
    public void fromWrongFormat_shouldThrows_ParserException(String from) {
        Exception exception = assertThrows(ParseException.class, () -> {
            statsService.get(from, null);
        });

        String expectedMessage = "Unparseable date";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @ParameterizedTest
    @CsvSource(value = {"12/05/2022:12/06/2022", "15/07/2022:16/07/2022", "1/08/1999:1/08/2000"}, delimiter = ':')
    public void withNotRegisterDays_shouldReturn_EmptyArray(String from, String to) throws ParseException {
        assertTrue(statsService.get(from, to).isEmpty());
    }

    @Test
    public void get_withTodayDate_AndToNull_returnTodayStats() throws ParseException {
        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        assertThat(statsService.get(formatter.format(today), null).size()).isEqualTo(1);
    }

    @Test
    public void get_withDayAfterToday_AndToNull_returnEmpty() throws ParseException {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        final Calendar from = Calendar.getInstance();
        from.setTime(new Date());
        from.add(Calendar.DATE, 5);

        assertThat(statsService.get(formatter.format(from.getTime()), null).size()).isEqualTo(0);
    }
}
