#!/bin/sh

cd $(dirname $0)

outdir="$1/"

mkdir -p $outdir
mkdir -p $outdir/resource

cp -var ./lib $outdir
cp -var ./dist $outdir
cp -var ./resource/dictionaries $outdir/resource
cp -var ./LETAdicts $outdir
cp -var ./Gazetteer $outdir
cp -var ./models $outdir

cp -va ./pipe.sh $outdir
cp -va ./pipe.bat $outdir
cp -va ./coref.prop_template $outdir/coref.prop
cp -va ./coref.prop $outdir 2>/dev/null #copy if exists
cp -va ./lv-ner-tagger.prop $outdir
