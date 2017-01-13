package users;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.users.Role;

/**
 * User class
 * This source is machine-generated from c8y-markdown docs.
 */
public class User extends C8yData {
	/**
	 * Uniquely identifies a user
	 * <pre>
	 * Allowed in PUT/POST request : not allowed
	 * Occurs : 1
	 * </pre>
	 */
	public String id;
	
	/**
	 * Link to this Resource
	 * <pre>
	 * Allowed in PUT/POST request : not allowed
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * User name, unique for a given domain. Max: 1000 characters
	 * <pre>
	 * Allowed in PUT/POST request : POST:mandatory PUT:not allowed
	 * Occurs : 1
	 * </pre>
	 */
	public String userName;
	
	/**
	 * User password. Min: 6, max: 32 characters. Only Latin1 chars allowed.
	 * <pre>
	 * Allowed in PUT/POST request : POST:mandatory PUT:optional
	 * Occurs : 1
	 * </pre>
	 */
	public String password;
	
	/**
	 * User first name.
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public String firstName;
	
	/**
	 * User last name.
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public String lastName;
	
	/**
	 * User phone number. Format: "+[country code][number]", has to be a valid
	 * MSISDN
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public String phone;
	
	/**
	 * User email address.
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public String email;
	
	/**
	 * User activation status (true/false)
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public boolean enabled;
	
	/**
	 * List of device permissions
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public JsonObject devicePermissions;
	
	/**
	 * List of all roles a current user has assignedÂ?(explicitly or implicitly
	 * via associated groups).
	 * <pre>
	 * Allowed in PUT/POST request : not allowed
	 * Occurs : 0..n
	 * </pre>
	 */
	public Role[] effectiveRoles;
	
}
