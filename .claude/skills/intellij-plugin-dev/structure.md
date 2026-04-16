# Plugin structure & `plugin.xml`

Official docs: <https://plugins.jetbrains.com/docs/intellij/plugin-structure.html>

## Project layout

```
src/main/
├── kotlin/...                         # production sources
└── resources/
    ├── META-INF/
    │   ├── plugin.xml                 # main descriptor
    │   └── <feature>.xml              # optional config-file includes
    ├── messages/
    │   └── <Bundle>.properties        # i18n
    ├── inspectionDescriptions/
    │   └── <shortName>.html           # one per <localInspection>
    └── icons/                         # SVG icons, addressable as /icons/foo.svg
```

## Minimal `plugin.xml`

```xml
<idea-plugin>
    <id>com.example.my</id>
    <name>My Plugin</name>
    <vendor email="me@example.com" url="https://example.com">Me</vendor>

    <depends>com.intellij.modules.platform</depends>
    <depends optional="true" config-file="language-php.xml">com.jetbrains.php</depends>

    <resource-bundle>messages.MyBundle</resource-bundle>

    <extensionPoints>
        <extensionPoint name="myEp" dynamic="true"
                        interface="com.example.my.MyEp"/>
    </extensionPoints>

    <extensions defaultExtensionNs="com.intellij">
        <!-- services, inspections, tool windows, etc. -->
    </extensions>

    <actions>
        <!-- AnAction registrations -->
    </actions>

    <applicationListeners>
        <!-- global listeners -->
    </applicationListeners>

    <projectListeners>
        <!-- per-project listeners -->
    </projectListeners>
</idea-plugin>
```

## Key attributes

- `<id>` — globally unique (reverse-DNS style).
- `<depends>`
  - `com.intellij.modules.platform` — required for every plugin.
  - `optional="true" config-file="..."` — loads extra XML **only if** the
    dependency is present. Use this for language-specific code (e.g. PHP).
- `<resource-bundle>` — default bundle for `@key=...` attributes in this
  descriptor (inspections, settings, notifications).
- `<extensionPoints>` vs `<extensions>`:
  - EPs you **define** go inside `<extensionPoints>`.
  - EPs you **contribute to** (yours or someone else's) go inside
    `<extensions defaultExtensionNs="...">`.

## Config-file splitting

Language/feature-specific code should live in a separate XML:

```xml
<!-- plugin.xml -->
<depends optional="true" config-file="language-php.xml">com.jetbrains.php</depends>
```

```xml
<!-- META-INF/language-php.xml -->
<idea-plugin>
    <extensions defaultExtensionNs="com.intellij">
        <localInspection language="PHP" .../>
    </extensions>
</idea-plugin>
```

If the optional plugin isn't installed, the file is simply not loaded — your
plugin still works without it.

## `pluginSinceBuild` & platform version

Declare the minimum IDE build in `intellijPlatform.pluginConfiguration.ideaVersion.sinceBuild`
(Gradle) — corresponds to `<idea-version since-build="...">` in generated
`plugin.xml`. Pin the concrete platform version in `gradle.properties`
(`platformVersion=2025.1.1`) so builds are reproducible.
