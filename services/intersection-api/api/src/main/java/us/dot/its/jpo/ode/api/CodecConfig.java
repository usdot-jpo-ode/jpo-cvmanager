package us.dot.its.jpo.ode.api;

import j2735ffm.MessageFrameCodec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodecConfig {

    @Value("${j2735.api.text-buffer-size}")
    private long textBufferSize;

    @Value("${j2735.api.uper-buffer-size}")
    private long uperBufferSize;

    @Value("${j2735.api.message-frame-allocate-size}")
    private long messageFrameAllocateSize;

    @Value("${j2735.api.asn-codec-ctx-max-stack-size}")
    private long asnCodecCtxMaxStackSize;

    @Bean
    public MessageFrameCodec messageFrameCodec() {
        System.out.println("Creating Message Codec Frame");
        System.out.println("Text buffer Size: " + textBufferSize);
        System.out.println("Uper buffer Size: " + uperBufferSize);
        System.out.println("Message Frame Allocate Size: " + messageFrameAllocateSize);
        System.out.println("ASN Codec Ctx Max Stack Size: " + asnCodecCtxMaxStackSize);

        return new MessageFrameCodec(
                textBufferSize,
                uperBufferSize,
                messageFrameAllocateSize,
                asnCodecCtxMaxStackSize);
    }
}
