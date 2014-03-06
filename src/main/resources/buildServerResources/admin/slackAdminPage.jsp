<h1>Slack Admin</h1>

<p>
    <b>Web Hook Token</b> : ${currentWebToken}
</p>


<p>
    <b>Default Channel</b> : ${defaultChannel}
</p>

<p>
    <b>Post URL :</b> ${postUrl}</br>
    <b>Logo URL :</b> ${logoUrl}</br>
</p>

<h2>Configuration</h2>
<p>There's no web ui for changing settings (yet) , so settings must be configured in the main config file, .BuildServer/config/main-config.xml - Add an element like so: <br />

<pre>
    &lt;server rootURL="http://localhost:8111"&gt;
        ...
        &lt;slackNotifier&gt;
            &lt;slackWebToken&gt;testToken2&lt;/slackWebToken&gt;
            &lt;slackDefaultChannel&gt;#testing&lt;/slackDefaultChannel&gt;
            &lt;slackPostUrl&gt;https://tapadoo.slack.com/services/hooks/incoming-webhook?token=&lt;/slackPostUrl&gt;
            &lt;slackLogoUrl&gt;http://build.tapadoo.com/img/icons/TeamCity32.png&lt;/slackLogoUrl&gt;
        &lt;/slackNotifier&gt;
        ...
        ...
    &lt;/server&gt;
</pre>

</p>
