package de.uniwue.controller;

import de.uniwue.db.config.HibernateUtil;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

@Controller
public class TourController {

    /**
     * Will fetch all the tours for the current url from the database and return them
     *
     * @return An array of all tour objects for the current url
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
                Supplier<Stream<Integer>> completedTourIdsSupplier = () -> Stream.of(completedToursCookie.split("---")).map(Integer::valueOf);
                toursForCurrentUrl.forEach(tour -> tour.setHasCompletedOnce(completedTourIdsSupplier.get().anyMatch(completedId -> completedId.equals(tour.getId()))));
            }

            if (!hiddenHotspotsCookie.isEmpty()) {
                Supplier<Stream<Integer>> hiddenHotspotIdsSupplier = () -> Stream.of(hiddenHotspotsCookie.split("---")).map(Integer::valueOf);
                toursForCurrentUrl.forEach(tour -> tour.getHotspot().setIsHidden(hiddenHotspotIdsSupplier.get().anyMatch(hiddenId -> hiddenId.equals(tour.getId()))));
            }

             /*
                Spring will convert all public fields (including getters) to JSON automatically.
                Example:

                Java List toursForCurrentUrl = List(
                    NormalSlide({
                        public getId(),
                        public getRelativeUrl(),
                        ...
                    }),
                    NormalSlide({...})
                )

                        |
                  will convert to
                       v

                Javascript Array = [
                    {
                        id: <output of getId()>
                        relativeUrl: <output of getRelativeUrl()>
                        ...
                    },
                    {...}
                ]

                That's why it's important to define a public getter in the entities, while keeping the fields private.
                Spring will automatically call all the getters and name the JS-Fields accordingly (getId() --> id)
             }*/
            return ResponseEntity.ok(toursForCurrentUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e);
        }
    }

}
