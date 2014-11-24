<p>
    <b>Will Post to slack :</b> ${enabled}<br />
    <b>Destination channel :</b> ${channel}<br />
    <b>Custom Logo Url (optional) :</b> ${logoUrl}<br />
</p>

<p>
    To disable for this project, or change channel: <br />
    Edit the plugin specific xml config, <code>plugin-settings.xml</code> probably somewhere inside <code>${configDir}</code><br />

    <pre>
&lt;settings&gt;
  &lt;slackSettings enabled="true"&gt;
    &lt;channel&gt;#blah&lt;/channel&gt;
    &lt;logoUrl&gt;http://link/to/logo.png&lt;/logoUrl&gt;
  &lt;/slackSettings&gt;
&lt;/settings&gt;
    </pre>
</p>