package org.inventivetalent.pluginannotations.command.exception;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PermissionException extends CommandException {

	private String permission;

	public PermissionException(String permission) {
		this.permission = permission;
	}

	public String getPermission() {
		return permission;
	}
}
