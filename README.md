# wicketmessages-maven-plugin

This is a maven plugin which converts wicket's properties.xml files into an excel file and back.

## About
For i18n in our wicket apps, we use *.properies.xml files like described in https://ci.apache.org/projects/wicket/guide/7.x/guide/i18n.html. Unfortunatelly it is a bit hard to keep the xml files synced between different locales. Another problem is that they are often not user friendly enough to be maintained by the business, so developers have to take care about translations by themsevles.
To address this problem we use this maven plugin which basically
 - scans sources for *.properties.xml files
 - merges them into an xlsx file with one column for every locale
 - reads updated xlsx and turns it into properties.xml files again
 
With the Excel file, it's easy to sort and filter keys, spot missing translations, and it's also more comfortable to use for the business department.

## Usage
### help
shows goals and possible parameters

```mvn com.pingunaut.maven.plugin:wicketmessages-maven-plugin:help```

### listFiles
lists all message files found in your maven project
#### parameters
- *fileExtension* default: .properties.xml
- *basedir* default: ${project.basedir}

```mvn com.pingunaut.maven.plugin:wicketmessages-maven-plugin:listFiles```

### generateXls
merges all message files found in your maven project into one xlsx file ([PROJECT_DIR]/messages.xlsx by default)
#### parameters
- *fileExtension* default: .properties.xml
- *basedir* default: ${project.basedir}
- *outputFile* default: messages.xlsx

```mvn com.pingunaut.maven.plugin:wicketmessages-maven-plugin:generateXls```

### generateXml
reads translations out of an excel file and writes them back into wicket's properties.xml files.
#### parameters
- *fileExtension* default: .properties.xml
- *basedir* default: ${project.basedir} 
- *inputFile* default: messages.xlsx // the excel file to read messages from


```mvn com.pingunaut.maven.plugin:wicketmessages-maven-plugin:generateXml```
