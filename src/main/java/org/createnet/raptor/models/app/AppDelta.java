package org.createnet.raptor.models.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AppDelta implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	final protected List<AppUser> userOldRoles = new ArrayList<AppUser>();
    final protected List<AppUser> deletedUsers = new ArrayList<AppUser>();
    final protected List<String> deleteDevices = new ArrayList<String>();
    
    public AppDelta() {
    }
    
	public List<AppUser> getUserOldRoles() {
		return userOldRoles;
	}
	public List<AppUser> getDeletedUsers() {
		return deletedUsers;
	}
	public List<String> getDeleteDevices() {
		return deleteDevices;
	}
    
}
