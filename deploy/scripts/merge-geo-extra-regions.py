#!/usr/bin/env python3
"""Добавляет в russia.geojson полигоны ДНР, ЛНР, Запорожской, Херсонской, Крыма и Севастополя."""
from __future__ import annotations

import json
import urllib.request
from copy import deepcopy
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GEO_PATHS = [
    ROOT / "frontend/public/geo/russia.geojson",
    ROOT / "custom-frontend/public/geo/russia.geojson",
]

UA_BASE = "https://raw.githubusercontent.com/EugeneBorshch/ukraine_geojson/master"
SEVASTOPOL_URL = "https://polygons.openstreetmap.fr/get_geojson.py?id=1574364&params=0"

EXTRA_REGIONS = [
    {
        "file": f"{UA_BASE}/UA_14_Donetska.geojson",
        "name": "Донецкая Народная Республика",
        "name_latin": "Donetsk People's Republic",
    },
    {
        "file": f"{UA_BASE}/UA_09_Luhanska.geojson",
        "name": "Луганская Народная Республика",
        "name_latin": "Luhansk People's Republic",
    },
    {
        "file": f"{UA_BASE}/UA_23_Zaporizka.geojson",
        "name": "Запорожская область",
        "name_latin": "Zaporizhzhia Oblast",
    },
    {
        "file": f"{UA_BASE}/UA_65_Khersonska.geojson",
        "name": "Херсонская область",
        "name_latin": "Kherson Oblast",
    },
    {
        "file": f"{UA_BASE}/UA_43_Avtonomna_Respublika_Krym.geojson",
        "name": "Республика Крым",
        "name_latin": "Republic of Crimea",
    },
]

REMOVE_NAMES = {item["name"] for item in EXTRA_REGIONS} | {"Севастополь"}


def fetch_json(url: str) -> dict:
    with urllib.request.urlopen(url, timeout=120) as response:
        return json.loads(response.read().decode("utf-8"))


def extract_geometry(payload: dict) -> dict:
    if payload.get("type") == "Feature":
        return payload["geometry"]
    if payload.get("type") == "FeatureCollection":
        return payload["features"][0]["geometry"]
    if payload.get("type") in ("Polygon", "MultiPolygon"):
        return payload
    raise ValueError(f"Unsupported geometry payload: {payload.get('type')}")


def make_feature(name: str, name_latin: str, geometry: dict, cartodb_id: int) -> dict:
    return {
        "type": "Feature",
        "properties": {
            "name": name,
            "cartodb_id": cartodb_id,
            "created_at": "2026-05-20T00:00:00+0000",
            "updated_at": "2026-05-20T00:00:00+0000",
            "name_latin": name_latin,
        },
        "geometry": geometry,
    }


def main() -> None:
    sevastopol_geom = extract_geometry(fetch_json(SEVASTOPOL_URL))
    next_id = 200

    new_features = []
    for spec in EXTRA_REGIONS:
        geometry = extract_geometry(fetch_json(spec["file"]))
        new_features.append(make_feature(spec["name"], spec["name_latin"], geometry, next_id))
        next_id += 1

    new_features.append(
        make_feature("Севастополь", "Sevastopol", sevastopol_geom, next_id),
    )

    for geo_path in GEO_PATHS:
        if not geo_path.exists():
            print(f"skip missing: {geo_path}")
            continue

        data = json.loads(geo_path.read_text(encoding="utf-8"))
        data["features"] = [
            feature
            for feature in data["features"]
            if feature.get("properties", {}).get("name") not in REMOVE_NAMES
        ]
        data["features"].extend(deepcopy(new_features))
        geo_path.write_text(
            json.dumps(data, ensure_ascii=False, separators=(",", ":")),
            encoding="utf-8",
        )
        print(f"updated {geo_path} -> {len(data['features'])} features")

    print("added:", ", ".join(REMOVE_NAMES))


if __name__ == "__main__":
    main()
