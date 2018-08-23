import com.sun.corba.se.spi.activation.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {
    public void bind(int port) throws Exception {
        //���÷���˵�NIO�߳���
        //ʵ����EventLoopGroup����Reactor�߳���
        //����Reactorһ�����ڷ���˽��տͻ��˵����ӣ���һ�����ڽ���SocketChannel�������д
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            /**
             * ��������ʹ���� NIO ���䣬����
             ��ָ�� NioEventLoopGroup���ܺʹ��������ӣ�ָ�� NioServerSocketChannel
             Ϊ�ŵ����͡��ڴ�֮���������ñ��ص�ַ�� InetSocketAddress ����ѡ��Ķ˿ڣ�6���硣
             ���������󶨵��˵�ַ�������µ���������
             */
            //ServerBootstrap������Netty��������NIO����˵ĸ��������࣬Ŀ���ǽ��ͷ���˿����ĸ��Ӷ�
            ServerBootstrap b = new ServerBootstrap();
            //Set the EventLoopGroup for the parent (acceptor) and the child (client).
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    //��I/O�¼��Ĵ�����ChildChannelHandler,����������Reactorģʽ�е�Handler��
                    //��Ҫ���ڴ�������I/O�¼��������¼��־������Ϣ���б�����
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        //���ServerHandler��Channel��ChannelPipeline
                        //ͨ��ServerHandler��ÿһ��������Channel��ʼ��
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ServerHandler());
                        }
                    });
            //�󶨼����˿ڣ�����syncͬ�����������ȴ��󶨲�����ɣ���ɺ󷵻�ChannelFuture������JDK��Future
            ChannelFuture f = b.bind(port).sync();
            //ʹ��sync���������������ȴ��������·�ر�֮��Main�������˳�
            f.channel().closeFuture().sync();
        }finally {
            //�����˳����ͷ��̳߳���Դ
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if(args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            }catch (NumberFormatException e) {
                //����Ĭ��ֵ
            }
        }
        new EchoServer().bind(port);
    }
    private class ServerHandler extends ChannelInboundHandlerAdapter {

        //ÿ����Ϣ��վ�������
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            System.out.println("The server receive msg :" + buf.toString());
            ctx.write(buf);
        }

        //֪ͨ����������channelread()�ǵ�ǰ�������е����һ����Ϣʱ����
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        //������ʱ�����쳣ʱ����
        @Override
        public void exceptionCaught (ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }
}