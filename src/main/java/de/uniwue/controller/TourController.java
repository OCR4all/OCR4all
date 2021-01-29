package de.uniwue.controller;

import de.uniwue.db.config.HibernateUtil;
import de.uniwue.db.entity.NormalSlide;
import de.uniwue.db.entity.Tour;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Stream;

@Controller
public class TourController {

    public static String findRootCause(Throwable throwable) {
        Objects.requireNonNull(throwable);
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage();
    }

    /**
     * Test
     *
     * @return A list of ids from the database
     */
    @RequestMapping(value = "/ajax/toursForCurrentUrl", method = RequestMethod.GET)
    public ResponseEntity<?> getAllToursForUrl(
            @RequestParam("url") String url,
            @CookieValue(value = "completedTours", defaultValue = "") String completedToursCookie,
            @CookieValue(value = "hiddenHotspots", defaultValue = "") String hiddenHotspotsCookie
    ) {
        try (Session session = HibernateUtil.getFactory().openSession()) {

            Query<Tour> query = session.createQuery("from Tour where relativeUrl = :url or relativeUrl is null", Tour.class);
            query.setParameter("url", url);
            List<Tour> toursForCurrentUrl = query.list();

            if (!completedToursCookie.isEmpty()) {
                Stream<Integer> completedTourIds = Stream.of(completedToursCookie.split("---")).map(Integer::valueOf);
                toursForCurrentUrl.forEach(tour -> tour.hasCompletedOnce = completedTourIds.anyMatch(completedId -> completedId.equals(tour.id)));
            }

            if (!hiddenHotspotsCookie.isEmpty()) {
                Stream<Integer> hiddenHotspotIds = Stream.of(hiddenHotspotsCookie.split("---")).map(Integer::valueOf);
                toursForCurrentUrl.forEach(tour -> tour.hotspot.isHidden = hiddenHotspotIds.anyMatch(hiddenId -> hiddenId.equals(tour.id)));
            }

            toursForCurrentUrl.forEach(tour -> {
                tour.normalSlides.sort(NormalSlide.normalSlideComparator);
            });

            return ResponseEntity.ok(toursForCurrentUrl);
        } catch (Exception e) {
            e.printStackTrace();
            // String rootCause = findRootCause(e);
            return ResponseEntity.status(500).body(e);
        }
    }

}
