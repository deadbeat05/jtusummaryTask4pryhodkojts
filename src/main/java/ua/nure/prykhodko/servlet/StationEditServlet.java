package ua.nure.prykhodko.servlet;

import org.apache.log4j.Logger;
import ua.nure.prykhodko.dao.SqlDAO.RouteDAO;
import ua.nure.prykhodko.dao.SqlDAO.StationDAO;
import ua.nure.prykhodko.entity.Station;
import ua.nure.prykhodko.exception.Messages;
import ua.nure.prykhodko.utils.TimeUtils;
import ua.nure.prykhodko.utils.Validation;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

@WebServlet("/admin/stationEdit/edit")
public class StationEditServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(StationEditServlet.class);

    @Override
    public void init() throws ServletException {
        log.info(Messages.INFO_ENTER);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext servletContext = req.getServletContext();
        StationDAO stationDAO = (StationDAO) servletContext.getAttribute("stationDAO");
        RouteDAO routeDAO = (RouteDAO) servletContext.getAttribute("routeDAO");
        final String station_name = req.getParameter("station_name");
        List<Station> stationList;
        if (req.getParameter("station_choose") == null) {
            stationList = stationDAO.getStationRoute(station_name);
            log.trace(Messages.TRACE_DELETE_ROUTE_FROM_STATION_ROUTE+stationList);
            if (stationList != null) {
                req.setAttribute("routeList", stationList);
                req.setAttribute("input", true);
                req.getRequestDispatcher("/jsp/EditStationPage.jsp").forward(req, resp);
            } else {
                req.setAttribute("input", false);
                req.getRequestDispatcher("/jsp/EditStationPage.jsp").forward(req, resp);
            }
        } else {
            String name = req.getParameter("name");
            int id = stationDAO.getEntityID(name);
            log.trace(Messages.TRACE_STATION_FOUND+id);

            String route_id = req.getParameter("route_id");
            String arrive_time = req.getParameter("arrive_time");
            String depart_time = req.getParameter("depart_time");
            req.setAttribute("station_chosen", true);
            req.setAttribute("input", true);
            req.setAttribute("id", id);
            req.setAttribute("route_id", route_id);
            req.setAttribute("name", name);
            req.setAttribute("arrive_time", arrive_time);
            req.setAttribute("depart_time", depart_time);
            req.getRequestDispatcher("/jsp/EditStationPage.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext servletContext = req.getServletContext();
        StationDAO stationDAO = (StationDAO) servletContext.getAttribute("stationDAO");
        RouteDAO routeDAO = (RouteDAO) servletContext.getAttribute("routeDAO");
        final String route_id = req.getParameter("route_id");
        final String id = req.getParameter("idOld");
        final String newName = req.getParameter("newName");
        final String newArriveDate = req.getParameter("newArriveDate");
        final String newDepartDate = req.getParameter("newDepartDate");
        final String newArriveTime = req.getParameter("NewArriveTime");
        final String newDepartTime = req.getParameter("NewDepartTime");
        boolean flag = false;
        Timestamp newArriveTimestamp = null;
        Timestamp newDepartTimestamp = null;
        Station stationOld;
        Station stationNew = new Station();

        if (!newArriveTime.equals("") && !newArriveDate.equals("")) {
            newArriveTimestamp = TimeUtils.parseStringToTimestamp(newArriveDate, newArriveTime);
        }

        if (!newDepartDate.equals("") && !newDepartTime.equals("")) {
            newDepartTimestamp = TimeUtils.parseStringToTimestamp(newDepartDate, newDepartTime);
        }

        stationOld = stationDAO.getEntityById(Integer.parseInt(id));
        stationOld.setRoute_id(Integer.parseInt(route_id));
        stationDAO.setStationTime(stationOld);
        stationNew.setId(Integer.parseInt(id));
        if (newName != null && !stationOld.getName().equals(newName) && !newName.equals("")) {
            if (Validation.isCorrectStationName(newName)) {
                if (newArriveTimestamp != null && newDepartTimestamp != null && newArriveTimestamp.getTime() - newDepartTimestamp.getTime() >=0) {
                    req.setAttribute("errorTime", true);
                    req.setAttribute("id", id);
                    req.setAttribute("route_id", route_id);
                    req.setAttribute("input", true);
                    req.setAttribute("station_chosen", true);
                    req.setAttribute("newName", newName);
                    flag = false;
                    req.getRequestDispatcher("/jsp/EditStationPage.jsp").forward(req, resp);
                } else {
                    stationNew.setName(newName);
                    stationDAO.update(stationNew);
                    flag = true;
                }
            } else {
                req.setAttribute("id", id);
                req.setAttribute("route_id", route_id);
                req.setAttribute("input", true);
                req.setAttribute("errorName", true);
                req.setAttribute("station_chosen", true);
                req.setAttribute("newName", newName);
                req.getRequestDispatcher("/jsp/EditStationPage.jsp").forward(req, resp);
            }
        }else{
            if (newArriveTimestamp != null && newDepartTimestamp != null && newArriveTimestamp.getTime() - newDepartTimestamp.getTime() >=0) {
                req.setAttribute("errorTime", true);
                req.setAttribute("id", id);
                req.setAttribute("newName", newName);
                req.setAttribute("route_id", route_id);
                req.setAttribute("input", true);
                req.setAttribute("station_chosen", true);
                flag = false;
                req.getRequestDispatcher("/jsp/EditStationPage.jsp").forward(req, resp);
            }
        }


            if (newArriveTimestamp != null && !stationOld.getArrive_time().equals(newArriveTimestamp)) {
                stationNew.setArrive_time(newArriveTimestamp);
                routeDAO.updateRouteArriveTime(Integer.parseInt(id), Integer.parseInt(route_id), stationNew.getArrive_time());
            }

            if (newDepartTimestamp != null && !stationOld.getDepart_time().equals(newDepartTimestamp)) {
                stationNew.setDepart_time(newDepartTimestamp);
                routeDAO.updateRouteDepartTime(Integer.parseInt(id), Integer.parseInt(route_id), stationNew.getDepart_time());
            }
        if (flag) {
            resp.sendRedirect("/admin");
        }
    }
    @Override
    public void destroy() {
        super.destroy();
    }

}
