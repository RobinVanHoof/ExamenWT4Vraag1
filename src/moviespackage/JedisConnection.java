package moviespackage;

import redis.clients.jedis.Jedis;

// Een singleton past erg goed bij een db connectie
public class JedisConnection {

	private static final String HOST = "localhost";
	private static JedisConnection instance;
	private Jedis connection;
	public int counter = 1;

	public static JedisConnection getInstance() {
		if (instance == null) {
			instance = new JedisConnection();
		}
		return instance;
	}

	private JedisConnection() {
		this.connection = new Jedis(HOST);
	}

	public Jedis getConnection() {
		return this.connection;
	}
}
