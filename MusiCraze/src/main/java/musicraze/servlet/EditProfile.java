package musicraze.servlet;

import musicraze.dal.*;
import musicraze.model.*;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.*;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/EditProfile")
public class EditProfile extends HttpServlet {

  private UsersDao usersDao;

  @Override
  public void init() throws ServletException {
    this.usersDao = UsersDao.getInstance();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    Users user = (Users) (req.getSession().getAttribute("user"));
    if (user == null) {
      res.sendRedirect("UserLogin");
      return;
    }
    req.setAttribute("inputs", this.generateDefaultInputs(user));
    req.getRequestDispatcher("/EditProfile.jsp").forward(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    Users user = (Users) (req.getSession().getAttribute("user"));
    if (user == null) {
      res.sendRedirect("UserLogin");
      return;
    }
    Map<String, String> alerts = new HashMap<>();
    String[] inputs = this.retrieveActualInputs(req);
    try {
      String firstName = this.validateLegalName(inputs[0]);
      String lastName = this.validateLegalName(inputs[1]);
      String email = this.validateEmail(inputs[2]);
      String avatar = this.validateAvatar(inputs[3]);
      String bio = inputs[4];
      Date bornDate = this.validateBornDate(inputs[5]);
      if (firstName == null) {
        alerts.put("firstName", "First name must contain only letters or spaces.");
      }
      if (lastName == null) {
        alerts.put("lastName", "Last name must contain only letters or spaces.");
      }
      if (email == null) {
        alerts.put("email", "Email must be formatted correctly.");
      }
      if (avatar == null) {
        alerts.put("avatar", "Avatar URL must be valid.");
      }
      if (bornDate == null) {
        alerts.put("bornDate",
            "Born date must be in format: yyyy-MM-dd. You must be 18+ to use MusiCraze.");
      }
      if (alerts.size() != 0) {
        throw new IllegalArgumentException();
      }
      if (!user.getFirstName().equals(firstName)) {
        user = this.usersDao.updateFirstName(user, firstName);
      }
      if (!user.getLastName().equals(lastName)) {
        user = this.usersDao.updateLastName(user, lastName);
      }
      if (!user.getEmail().equals(email)) {
        user = this.usersDao.updateEmail(user, email);
      }
      if (!user.getAvatar().equals(avatar)) {
        user = this.usersDao.updateAvatar(user, avatar);
      }
      if (!user.getBio().equals(bio)) {
        user = this.usersDao.updateBio(user, bio);
      }
      if (!user.getBornDate().equals(bornDate)) {
        user = this.usersDao.updateBornDate(user, bornDate);
      }
      req.setAttribute("user", user);
      req.getSession().setAttribute("user", user);
      res.sendRedirect("UserProfile");
    } catch (IllegalArgumentException e) {
      req.setAttribute("alerts", alerts);
      req.setAttribute("inputs", inputs);
      req.getRequestDispatcher("/EditProfile.jsp").forward(req, res);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new IOException(e);
    }
  }
  
  private String[] generateDefaultInputs(Users user) {
    String[] inputs = new String[6];
    inputs[0] = user.getFirstName();
    inputs[1] = user.getLastName();
    inputs[2] = user.getEmail();
    inputs[3] = user.getAvatar();
    inputs[4] = user.getBio();
    inputs[5] = user.getBornDateStr();
    return inputs;
  }

  private String[] retrieveActualInputs(HttpServletRequest req) {
    String[] inputs = new String[6];
    inputs[0] = this.trimString(req.getParameter("firstName"));
    inputs[1] = this.trimString(req.getParameter("lastName"));
    inputs[2] = this.trimString(req.getParameter("email"));
    inputs[3] = this.trimString(req.getParameter("avatar"));
    inputs[4] = this.trimString(req.getParameter("bio"));
    inputs[5] = this.trimString(req.getParameter("bornDate"));
    return inputs;
  }

  private String trimString(String str) {
    return str == null ? "" : str.trim();
  }

  private String validateLegalName(String legalName) {
    if (legalName.matches("^[ A-Za-z]+$")) {
      return legalName;
    }
    return null;
  }

  private String validateEmail(String email) {
    if (email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      return email;
    }
    return null;
  }

  private String validateAvatar(String avatar) {
    try {
      if (ImageIO.read(new URL(avatar)) != null) {
        return avatar;
      }
    } catch (Exception ignored) {
    }
    return null;
  }

  private Date validateBornDate(String bornDate) {
    try {
      if (Period.between(LocalDate.parse(bornDate), LocalDate.now()).getYears() >= 18) {
        return new SimpleDateFormat("yyyy-MM-dd").parse(bornDate);
      }
    } catch (Exception ignored) {
    }
    return null;
  }
}
