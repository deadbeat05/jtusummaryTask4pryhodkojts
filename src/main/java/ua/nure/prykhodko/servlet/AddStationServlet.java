package ua.nure.prykhodko.servlet;

import ua.nure.prykhodko.dao.SqlDAO.RouteDAO;
import ua.nure.prykhodko.dao.SqlDAO.StationDAO;
import ua.nure.prykhodko.entity.Station;
import ua.nure.prykhodko.utils.TimeUtils;
import ua.nure.prykhodko.utils.Validation;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;

@WebServlet("/admin/stationEdit/add")
public class AddStationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext servletContext = req.getServletContext();
        StationDAO stationDAO = (StationDAO) servletContext.getAttribute("stationDAO");
        RouteDAO routeDAO = (RouteDAO) servletContext.getAttribute("routeDAO");
        Station station = new Station();
        int id = 0;
        boolean flagRoute = true;
        final Timestamp depart_date;
        final Timestamp arrive_date;
        final String name = req.getParameter("name");
        final String departDate = req.getParameter("depart_date");
        final String departTime = req.getParameter("depart_time");
        final String arriveDate = req.getParameter("arrive_date");
        final String arriveTime = req.getParameter("arrive_time");
        final String route = req.getParameter("route");
        int route_id=0;

        if (!route.equals("")) {
            route_id = Integer.parseInt(route);
        }else {
            req.setAttribute("errorRoute", "Incorrect route id");
            flagRoute = false;
        }
        if (!name.equals("") && Validation.isCorrectStationName(name)) {
            station.setName(name);
            stationDAO.addEntity(station);
            id = stationDAO.getEntityID(station.getName());
        } else {
            req.setAttribute("errorName", "Incorrect name");
            flagRoute = false;
        }
        if (!departDate.equals("") && !departTime.equals("")) {
            depart_date = TimeUtils.parseStringToTimestamp(req.getParameter("depart_date"), req.getParameter("depart_time"));
            station.setDepart_time(depart_date);
        }

        if (!arriveDate.equals("") && !arriveTime.equals("")) {
            arrive_date = TimeUtils.parseStringToTimestamp(req.getParameter("arrive_date"), req.getParameter("arrive_time"));
            station.setArrive_time(arrive_date);
        }

        if (id > 0 && route_id > 0) {
            station.setRoute_id(route_id);
            station.setId(id);
            routeDAO.addStationToRoute(station);
            req.setAttribute("success", true);
            req.getRequestDispatcher("/jsp/AddStationPage.jsp").forward(req, resp);
        } else {
            req.setAttribute("error", true);
            req.setAttribute("station_name", name);
            req.setAttribute("route_id", route_id);
            req.getRequestDispatcher("/jsp/AddStationPage.jsp").forward(req, resp);
        }
    }
}