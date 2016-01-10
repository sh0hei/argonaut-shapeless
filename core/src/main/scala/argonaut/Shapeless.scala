package argonaut

import shapeless.Witness
import shapeless.compat.{ Cached, Widen, Lazy, Strict, LowPriority }
import argonaut.derive._

trait SingletonInstances {

  implicit def singletonTypeEncodeJson[S, W >: S]
   (implicit
     w: Witness.Aux[S],
     widen: Widen.Aux[S, W],
     underlying: EncodeJson[W]
   ): EncodeJson[S] =
    underlying.contramap[S](widen.apply)

  implicit def singletonTypeDecodeJson[S, W >: S]
   (implicit
     w: Witness.Aux[S],
     widen: Widen.Aux[S, W],
     underlying: DecodeJson[W]
   ): DecodeJson[S] =
    DecodeJson { c =>
      underlying.decode(c).flatMap { w0 =>
        if (w0 == w.value)
          DecodeResult.ok(w.value)
        else
          DecodeResult.fail(s"Expected ${w.value}, got $w0", c.history)
      }
    }

}

trait DerivedInstances {

  implicit def mkEncodeJson[T]
   (implicit
     ev: Strict[LowPriority[EncodeJson[T]]],
     mkEncode: Strict[MkEncodeJson[T]]
   ): EncodeJson[T] =
    mkEncode.value.encodeJson

  implicit def mkDecodeJson[T]
   (implicit
     ev: Strict[LowPriority[DecodeJson[T]]],
     priority: Strict[MkDecodeJson[T]]
   ): DecodeJson[T] =
    priority.value.decodeJson

}

trait CachedDerivedInstances {

  implicit def mkEncodeJson[T]
   (implicit
     ev: Cached[Strict[LowPriority[EncodeJson[T]]]],
     priority: Cached[Strict[MkEncodeJson[T]]]
   ): EncodeJson[T] =
    priority.value.value.encodeJson

  implicit def mkDecodeJson[T]
   (implicit
     ev: Cached[Strict[LowPriority[DecodeJson[T]]]],
     priority: Cached[Strict[MkDecodeJson[T]]]
   ): DecodeJson[T] =
    priority.value.value.decodeJson

}

object Shapeless
  extends SingletonInstances
  with DerivedInstances {

  object Cached
    extends SingletonInstances
    with CachedDerivedInstances

}
