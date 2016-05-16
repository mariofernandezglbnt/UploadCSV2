Upload CSV
---------------------------------------

Para ejecutar la aplicación se puede hacer de esta forma y obtendremos en consola los mensajes de log:

C:\Java>java -jar uploadCSV.jar

Como vemos, la ejecucion se hace a traves de la JVM y para que la misma tenga el certificado que necesitemos (si necesitamos conectar con sitios HTTPS) hay que instalarlo.
Para instalar certificados podemos hacerlo de la siguiente manera, utilizando keytool que viene en Java. En este ejemplo se va a instalar el certificado de StarmeUp:

C:\Java>keytool -import -alias starmeup -file starmeup.cer

Se pueden ver los certificados instalador en nuestro keyStore con:

C:\Java>keytool -list
Enter keystore password:

Keystore type: JKS
Keystore provider: SUN

Your keystore contains 2 entries

starmeup, 03/05/2016, trustedCertEntry,
Certificate fingerprint (SHA1): 18:91:31:3F:54:B6:B4:C5:13:8C:D7:8A:F5:9E:29:A1:1D:A2:C3:4E
verisign, 03/05/2016, trustedCertEntry,
Certificate fingerprint (SHA1): 89:F5:E2:CA:A3:43:07:09:71:C6:34:D5:24:41:69:46:98:B8:1C:11

La aplicacion necesita que se le indique en donde esta el keystore. El certificado instalado con el paso anterior queda normalmente en un keystore del usuario logueado en el sistema.
Por ejemplo, si tu sistema es Windows el keystore estará en:

C:\Users\Nombre.Usuario\.keystore