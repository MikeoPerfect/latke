/*
 * Copyright (c) 2009-2018, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.latke.cron;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimerTask;

/**
 * A cron job is a scheduled task, it will invoke {@link #url a URL} via an HTTP GET request, at a given time of day.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.2.2, Aug 4, 2018
 */
public final class Cron extends TimerTask {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Cron.class);

    /**
     * Time unit constant - 10.
     */
    public static final int TEN = 10;

    /**
     * Time unit constant - 60.
     */
    public static final int SIXTY = 60;

    /**
     * Time unit constant - 1000.
     */
    public static final int THOUSAND = 1000;

    /**
     * The URL this cron job to invoke.
     */
    private String url;

    /**
     * Description of this cron job.
     */
    private String description;

    /**
     * Schedule of this cron job.
     *
     * <p>
     * Available format: <em>every N (hours|minutes|seconds)</em>, for examples:
     * <ul>
     * <li>every 12 hours</li>
     * <li>every 10 minutes</li>
     * <li>every 30 seconds</li>
     * </ul>
     * </p>
     */
    private String schedule;

    /**
     * Time in milliseconds between successive task executions.
     */
    private long period;

    /**
     * Timeout of this cron job executing.
     */
    private int timeout;

    /**
     * Constructs a cron job with the specified URL, description, schedule and timeout.
     *
     * @param url         the specified URL
     * @param description the specified description
     * @param schedule    the specified schedule
     * @param timeout     the specified timeout
     */
    public Cron(final String url, final String description, final String schedule, final int timeout) {
        this.url = url;
        this.description = description;
        this.schedule = schedule;
        this.timeout = timeout;

        parse(schedule);
    }

    @Override
    public void run() {
        LOGGER.debug("Executing scheduled task....");

        try {
            final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            final StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            LOGGER.log(Level.DEBUG, "Executed scheduled task [url=" + url + ", response=" + content + "]");
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Scheduled task execute failed [" + url + "]", e);
        }
    }

    /**
     * Parses the specified schedule into {@link #period execution period}.
     *
     * @param schedule the specified schedule
     */
    private void parse(final String schedule) {
        final int num = Integer.valueOf(StringUtils.substringBetween(schedule, " ", " "));
        final String timeUnit = StringUtils.substringAfterLast(schedule, " ");

        LOGGER.log(Level.TRACE, "Parsed a cron job [schedule={0}]: [num={1}, timeUnit={2}, description={3}], ",
                new Object[]{schedule, num, timeUnit, description});

        if ("hours".equals(timeUnit)) {
            period = num * SIXTY * SIXTY * THOUSAND;
        } else if ("minutes".equals(timeUnit)) {
            period = num * SIXTY * THOUSAND;
        } else if ("seconds".equals(timeUnit)) {
            period = num * THOUSAND;
        }
    }

    /**
     * Gets the period.
     *
     * @return period
     */
    public long getPeriod() {
        return period;
    }

    /**
     * Gets the description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the schedule.
     *
     * @return schedule
     */
    public String getSchedule() {
        return schedule;
    }

    /**
     * Gets the URL.
     *
     * @return URL
     */
    public String getURL() {
        return url;
    }

    /**
     * Sets the URL with the specified URL.
     *
     * @param url the specified URL
     */
    public void setURL(final String url) {
        this.url = url;
    }
}
