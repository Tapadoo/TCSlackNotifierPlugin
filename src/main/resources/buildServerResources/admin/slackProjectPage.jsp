<p>
    <b>Will Post to slack :</b> ${enabled}<br />
    <b>Destination channel :</b> ${channel}<br />
</p>

<p>
    To disable for this project, or change channel: <br />
    Edit the plugin specific xml config, <code>plugin-settings.xml</code> probably somewhere inside <code>${configDir}</code><br />

    <pre>
&lt;settings&gt;
  &lt;slackSettings enabled="true"&gt;
    &lt;channel&gt;#blah&lt;/channel&gt;
  &lt;/slackSettings&gt;
&lt;/settings&gt;
    </pre>
</p>