@echo off


set dest_file_1=kk-duktape\kk-duktape\src\main\jni\duktape
set dest_file_2=kk-app\demo\app\src\main\assets\main
set dest_file_3=kk-app\demo\kk-app
set dest_file_4=kk-app\demo\kk-duktape
set dest_file_5=kk-app\demo\kk-http
set dest_file_6=kk-app\demo\kk-observer
set dest_file_7=kk-app\demo\kk-script
set dest_file_8=kk-app\demo\kk-view

set source_file_1=kk-duktape\lib\duktape
set source_file_2=fruit\main
set source_file_3=kk-app\kk-app
set source_file_4=kk-duktape\kk-duktape
set source_file_5=kk-http\kk-http
set source_file_6=kk-observer\kk-observer
set source_file_7=kk-script\kk-script
set source_file_8=kk-view\kk-view


echo welcome to use bat script
echo 1. delete before linux soft link file or windows Symbolic-Links 


IF EXIST %dest_file_1%\*.* (

rd /s /q %dest_file_1%
rd /s /q %dest_file_2%
rd /s /q %dest_file_3%
rd /s /q %dest_file_4%
rd /s /q %dest_file_5%
rd /s /q %dest_file_6%
rd /s /q %dest_file_7%
rd /s /q %dest_file_8%

echo 1.1 delete symbol-link sucess ....
) ELSE (

del /q %dest_file_1%
del /q %dest_file_2%
del /q %dest_file_3%
del /q %dest_file_4%
del /q %dest_file_5%
del /q %dest_file_6%
del /q %dest_file_7%
del /q %dest_file_8%

echo 1.1 delete linux link file sucess .....
)



echo 2. create new Symbolic-Links

rem 相对路径

mklink /d %dest_file_1% ..\..\..\..\..\%source_file_1%
mklink /d %dest_file_2% ..\..\..\..\..\..\%source_file_2%
mklink /d %dest_file_3% ..\..\%source_file_3%
mklink /d %dest_file_4% ..\..\%source_file_4%
mklink /d %dest_file_5% ..\..\%source_file_5%
mklink /d %dest_file_6% ..\..\%source_file_6%
mklink /d %dest_file_7% ..\..\%source_file_7%
mklink /d %dest_file_8% ..\..\%source_file_8%


pause