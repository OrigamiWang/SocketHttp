# 踩坑记录

> readline()方法最后需要换行符，否则会阻塞
> BufferedInputStream.read()也不能在http中使用，因为该方法会阻塞主线程， 并没有像往常那样，当调用该方法读到文档末尾时，
 会返回-1，只有关闭socket时才会使主线程不会阻塞，
 但是这样就不满足长连接的需求了 available（）方法可返回BufferedInputStream中字节长度，
 那么我们就可以通过记录每次读取的长度，然后每一次循环时都和总长度比较， 当读取的长度大于等于BufferedInputStream中的字节长度就跳出循环即可
> 记得关闭输出流，否则也会阻塞 