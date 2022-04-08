package musicraze.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import musicraze.dal.PlaylistsDao;
import musicraze.model.PlaylistSongContains;
import musicraze.model.Playlists;

@WebServlet("/PlaylistUpdateInfo")
public class PlaylistUpdateInfo extends HttpServlet{
	protected PlaylistsDao playlistsDao; 
	
    @Override
    public void init() throws ServletException {
	  playlistsDao = PlaylistsDao.getInstance();
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
      String playlistIdStr = req.getParameter("playlistId");
      int playlistId = Integer.parseInt(playlistIdStr);
      
      req = getRenderPlaylistInfo(req, playlistId);
      
      
      req.getRequestDispatcher("PlaylistUpdateInfo.jsp").forward(req, res);
    }
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
      // Manage Update Info logic, and re-render after submission
      String playlistIdStr = req.getParameter("playlistId");
      int playlistId = Integer.parseInt(playlistIdStr);
      Playlists playlist = null;
      
      try {
    	  playlist = playlistsDao.getPlaylistById(playlistId);
	  } catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e);
	  }
      
      String newPlaylistName = req.getParameter("newPlaylistName");
      String newDescription = req.getParameter("newDescription");
      
          
      System.out.println("new Description:" + newDescription);
      System.out.println("new Name:" + newPlaylistName);
      
      

      try {

    	  playlistsDao.updatePlaylistName(playlist, newPlaylistName);
    	

		  playlistsDao.updateDescription(playlist, newDescription);
    	
	  } catch (SQLException e) {
		e.printStackTrace();
		throw new IOException(e);
	  }
      
      req = getRenderPlaylistInfo(req, playlistId);
      req.getRequestDispatcher("PlaylistUpdateInfo.jsp").forward(req, res);

    }
    
    
    public HttpServletRequest getRenderPlaylistInfo(HttpServletRequest req, int playlistId) throws IOException {
    	String playlistName = null;
    	String description = null;

    	try {
			Playlists playlist = playlistsDao.getPlaylistById(playlistId);
			playlistName = playlist.getPlaylistName();
			description = playlist.getDescription();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
    	req.setAttribute("playlistId", playlistId);
    	req.setAttribute("playlistName", playlistName);
    	req.setAttribute("description", description);
    	return req;
    }
    

}

