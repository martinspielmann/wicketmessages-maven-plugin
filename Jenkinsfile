node {
   def mvnHome
   stage('Preparation') {
      git 'https://github.com/pingunaut/wicketmessages-maven-plugin.git'
      mvnHome = tool 'M3'
   }
   stage('Build') {
      if (isUnix()) {
         sh "'${mvnHome}/bin/mvn' clean install"
      } else {
         bat(/"${mvnHome}\bin\mvn" clean install/)
      }
   }
   
}
